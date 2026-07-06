package org.lucas.arbackend.service.vr;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.vr.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.course.ChapterSection;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.vr.VREvent;
import org.lucas.arbackend.entity.vr.VRSession;
import org.lucas.arbackend.mapper.VRSessionMapper;
import org.lucas.arbackend.repository.course.ChapterSectionRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.repository.vr.VREventRepository;
import org.lucas.arbackend.repository.vr.VRSessionRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VRSessionService {

    private final VRSessionRepository sessionRepo;
    private final VREventRepository eventRepo;
    private  final StudentRepository studentRepo;
    private final ChapterSectionRepository sectionRepo;
    private final TenantProvider tenantProvider;
    private final VRSessionMapper sessionMapper;

    // ==========================================
    // SESSION LIFECYCLE
    // ==========================================

    public VRSessionResponse startSession(String studentNumber, VRSessionStartRequest request) {
        Organisation org = tenantProvider.getOrg();

        Student student = studentRepo.findByOrganisationIdAndStudentNumber(org.getId(), studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("No student found with number: " + studentNumber));

        ChapterSection section = sectionRepo.findWithContext(request.getCourseId(), request.getChapterId(), org.getId(), request.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("No section found with ID: " + request.getSectionId()));

        List<VRSession> activeSession = sessionRepo.findAllByStudentStudentNumberAndOrganisationId(student.getStudentNumber(), org.getId());
        if (!activeSession.isEmpty()) {
            log.warn("Student [{}] has [{}] active session, ending it before starting a new one", student.getStudentNumber(), activeSession.size());
            activeSession.forEach(this::forceEndSession);
        }

        VRSession session = VRSession
                .builder()
                .student(student)
                .chapterSection(section)
                .organisation(org)
                .deviceId(request.getDeviceId())
                .headsetModel(request.getHeadsetModel())
                .startedAt(LocalDateTime.now())
                .frameDropCount(0)
                .trackingLossCount(0)
                .interactionCount(0)
                .hintRequestCount(0)
                .failureCount(0)
                .build();

        log.info("VR Session started: [{}] for student [{}], chapter section [{}]", session.getId(), student.getStudentNumber(), section.getTitle());
        return sessionMapper.toResponse(sessionRepo.save(session));
    }

    public void forceEndSession(VRSession session) {
        session.setEndedAt(LocalDateTime.now());
        session.setDurationSeconds((int) ChronoUnit.SECONDS.between(session.getStartedAt(), session.getEndedAt()));
        sessionRepo.save(session);
        log.info("Force-ended stale VR session [{}]", session.getId());
    }

    public void endSession(Long sessionId, VRSessionEndRequest request) {
        Long orgId = tenantProvider.get();
        VRSession session = sessionRepo.findByIdAndOrganisationId(sessionId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("No session found with ID: " + sessionId));

        session.setEndedAt(LocalDateTime.now());
        session.setDurationSeconds((int) ChronoUnit.SECONDS.between(session.getStartedAt(), session.getEndedAt()));
        session.setComfortRating(request.getComfortRating());
        session.setMotionSicknessReported(request.getMotionSicknessReported());
        session.setAvgFps(request.getAvgFps());
        session.setFrameDropCount(request.getFrameDropCount());
        session.setTrackingLossCount(request.getTrackingLossCount());
        session.setInteractionCount(request.getInteractionCount());
        session.setHintRequestCount(request.getHintRequestCount());
        session.setFailureCount(request.getFailureCount());
        session.setCompletionConditionMet(request.getCompletionConditionMet());
        session.setCompletionTimeMs(request.getCompletionTimeMs());

        BigDecimal qualityScore = computeSessionQualityScore(session);
        session.setSessionQualityScore(qualityScore);

        sessionRepo.save(session);
        log.info("VR Session ended: [{}] for student [{}] with a quality score of [{}]", session.getId(), session.getStudent(), qualityScore);
    }

    private BigDecimal computeSessionQualityScore (VRSession session) {
        BigDecimal score = BigDecimal.ONE;

        if (session.getAvgFps() != null && session.getAvgFps().compareTo(BigDecimal.valueOf(72)) < 0) {
            score = score.subtract(BigDecimal.valueOf(0.2));
        }
        if (session.getFrameDropCount() != null && session.getFrameDropCount() > 10) {
            score = score.subtract(BigDecimal.valueOf(0.1));
        }
        if (session.getTrackingLossCount() != null && session.getTrackingLossCount() > 0) {
            score = score.subtract(BigDecimal.valueOf(0.15));
        }
        if (session.getMotionSicknessReported() != null && session.getMotionSicknessReported()) {
            score = score.subtract(BigDecimal.valueOf(0.1));
        }
        if (session.getCompletionConditionMet() != null && session.getCompletionConditionMet()) {
            score = score.add(BigDecimal.valueOf(0.1));
        }

        return score.max(BigDecimal.ZERO).min(BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP);
    }

    // ==========================================
    // EVENT RECORDING
    // ==========================================

    public void batchRecordingEvents(Long sessionId, List<VREventRequest> events) {
        Organisation org = tenantProvider.getOrg();

        VRSession session = sessionRepo.findByIdAndOrganisationId(sessionId, org.getId())
                .orElseThrow(() -> new EntityNotFoundException("No session found with ID: " + sessionId));

        List<VREvent> entities = events.stream()
                .map(req -> VREvent.builder()
                .session(session)
                .organisation(org)
                .eventType(req.getEventType())
                .timestamp(req.getTimestamp())
                .positionX(req.getPositionX())
                .positionY(req.getPositionY())
                .positionZ(req.getPositionZ())
                .rotationX(req.getRotationX())
                .rotationY(req.getRotationY())
                .rotationZ(req.getRotationZ())
                .targetObjectId(req.getTargetObjectId())
                .durationInMilliseconds(req.getDurationMs())
                .metadataJson(req.getMetadataJson())
                .hand(req.getHand())
                .sequenceNumber(req.getSequenceNumber())
                .build()).collect(Collectors.toList());

        eventRepo.saveAll(entities);
        log.debug("Recorded {} events for session {}", entities.size(), sessionId);
    }

    // ==========================================
    // QUERY OPERATIONS
    // ==========================================

    @Transactional(readOnly = true)
    @Cacheable(value = "vr_session", key = "#sessionId", unless = "#result == null")
    public VRSessionResponse getSession(Long sessionId) {
        VRSession session = sessionRepo.findByIdAndOrganisationId(sessionId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No session found with ID: " + sessionId));
        return sessionMapper.toResponse(session);
    }

    @Transactional(readOnly = true)
    public Page<VRSessionResponse> getStudentSessions(String StudentNumber, Pageable pageable) {
        return sessionRepo.findAllByStudentStudentNumberAndOrganisationId(StudentNumber, tenantProvider.get(), pageable)
                .map(sessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VRSessionResponse> getAllSessions(Pageable pageable) {
        return sessionRepo.findAllByOrganisationId(tenantProvider.get(), pageable)
                .map(sessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<VREventResponse> getSessionEvents(Long sessionId, Pageable pageable) {
        VRSession session = sessionRepo.findByIdAndOrganisationId(sessionId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No session found with ID: " + sessionId));

        return eventRepo.findAllBySessionIdAndOrganisationId(sessionId, tenantProvider.get(), pageable)
                .map(sessionMapper::toEventResponse);
    }

    @Transactional(readOnly = true)
    public List<VREventResponse> getEventsByType(Long sessionId, String eventType) {
        return eventRepo.findAllBySessionIdAndOrganisationIdAndEventTypeOrderByTimestampAsc(
                        sessionId, tenantProvider.get(), eventType)
                .stream()
                .map(sessionMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    // ==========================================
    // ANALYTICS HELPERS
    // ==========================================

    @Transactional(readOnly = true)
    public VRStudentAnalyticsResponse getStudentAnalytics(String studentNumber) {
        Long orgId = tenantProvider.get();
        long totalSessions = sessionRepo.countCompletedSessions(studentNumber, orgId);
        Double avgQuality = sessionRepo.calculateAverageQualityScore(studentNumber, orgId);
        long motionSicknessCount = sessionRepo.countMotionSicknessReports(studentNumber, orgId);

        return VRStudentAnalyticsResponse.builder()
                .studentNumber(studentNumber)
                .averageQualityScore(avgQuality != null ? BigDecimal.valueOf(avgQuality) : null)
                .motionSicknessReports(motionSicknessCount)
                .totalCompletedSessions(totalSessions)
                .build();
    }
}
