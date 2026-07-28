package org.lucas.arbackend.service.vr;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.competency.*;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.vr.VRSession;
import org.lucas.arbackend.entity.vr.competency.Competency;
import org.lucas.arbackend.entity.vr.competency.CompetencyAssessment;
import org.lucas.arbackend.entity.vr.competency.CompetencyCriterion;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.mapper.vr.CompetencyMapper;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.repository.vr.CompetencyAssessmentRepository;
import org.lucas.arbackend.repository.vr.CompetencyRepository;
import org.lucas.arbackend.repository.vr.VRSessionRepository;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompetencyService {
    private final TenantProvider tenantProvider;
    private final StudentRepository studentRepo;
    private final VRSessionRepository sessionRepo;
    private final CompetencyRepository competencyRepo;
    private final CompetencyAssessmentRepository assessmentRepo;
    private final CompetencyMapper competencyMapper;

    public CompetencyResponse createCompetency(CompetencyCreateRequest request) {
        // TODO: Make sure that it populates the organisation id correctly.
        // If not then set it manually with the .builder()
        Organisation organisation = tenantProvider.getOrg();
        MappingContext ctx = new MappingContext(organisation, null, null);
        Competency competency = competencyMapper.updateCompetency(request,new Competency(), ctx);

        return competencyMapper.toCompetencyResponse(competencyRepo.save(competency));
    }

    public CompetencyResponse addCriterion(Long competencyId, CompetencyCriterionRequest request) {
        Long orgId = tenantProvider.get();

        Competency competency = competencyRepo.findByIdAndOrganisationId(competencyId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("No criterion found with id: " + competencyId));

        CompetencyCriterion criterion = CompetencyCriterion
                .builder()
                .competency(competency)
                .build();

        competencyMapper.updateCriterion(request, criterion);
        return competencyMapper.toCompetencyResponse(competencyRepo.save(competency));
    }

    public CompetencyAssessmentResponse recordAssessment(String studentNumber, Long competencyId, CompetencyAssessmentCreateRequest request) {
        Long orgId = tenantProvider.get();

        Competency competency = competencyRepo.findByIdAndOrganisationId(competencyId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("No criterion found with id: " + competencyId));
        Student student = studentRepo.findByOrganisationIdAndStudentNumber(orgId, studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("No student found with student number: " + studentNumber));
        VRSession session = sessionRepo.findByIdAndOrganisationId(request.getSessionId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("No session found with id: " + request.getSessionId()));

        CompetencyAssessment assessment = CompetencyAssessment
                .builder()
                .session(session)
                .competency(competency)
                .student(student)
                .build();

        competencyMapper.updateAssessment(request, assessment);

        return competencyMapper.toAssessmentResponse(assessmentRepo.save(assessment));
    }

    public StudentCompetencyDashboardResponse getStudentCompetencyDashBoard(String studentNumber)  {
        List<CompetencyAssessment> assessments = assessmentRepo.findByStudent_StudentNumberOrderByAssessedAtDesc(studentNumber);

        long totalAssessed = assessments.size();
        long totalPassed = assessments.stream().filter(CompetencyAssessment::getPassed).count();
        double passRate = totalAssessed == 0 ? 0.0 : ((double) totalPassed / totalAssessed) * 100;

        List<CompetencyAssessmentResponse> recentResponses = assessments.stream()
                .limit(10)
                .map(competencyMapper::toAssessmentResponse)
                .toList();

        return StudentCompetencyDashboardResponse
                .builder()
                .studentNumber(studentNumber)
                .totalCompetenciesAssessed(totalAssessed)
                .totalPassed(totalPassed)
                .overallPassRatePercentage(Math.round(passRate * 100.0) / 100.0)
                .recentAssessments(recentResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<CompetencyResponse> getAllCompetencies(Pageable pageable) {
        Long orgId = tenantProvider.get();
        return competencyRepo.findAllByOrganisationId(orgId, pageable)
                .map(competencyMapper::toCompetencyResponse);
    }

    @Transactional(readOnly = true)
    public List<CompetencyResponse> getCompetenciesForScene(Long sceneId) {
        Long orgId = tenantProvider.get();
        return competencyRepo.findAllByAssociatedSceneIdAndOrganisationId(sceneId, orgId)
                .stream()
                .map(competencyMapper::toCompetencyResponse)
                .toList();
    }
}
