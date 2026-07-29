package org.lucas.arbackend.repository.vr;

import org.lucas.arbackend.entity.vr.event.VREvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<VREvent, Long> {

    Page<VREvent> findAllBySessionIdAndOrganisationId(Long sessionId, Long organisationId, Pageable pageable);

    List<VREvent> findAllBySessionIdAndOrganisationIdAndEventTypeOrderByTimestampAsc(
            Long sessionId, Long orgId, String eventType);

    @Query("SELECT e FROM VREvent e WHERE e.session.id = :sessionId AND e.organisation.id = :orgId AND e.timestamp BETWEEN :start AND :end ORDER BY e.timestamp ASC")
    List<VREvent> findEventsInTimeRange(
            @Param("sessionId") Long sessionId,
            @Param("orgId") Long orgId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT e FROM VREvent e WHERE e.session.id = :sessionId AND e.organisation.id = :orgId AND e.targetObjectId = :objectId ORDER BY e.timestamp ASC")
    List<VREvent> findEventsByTargetObject(
            @Param("sessionId") Long sessionId,
            @Param("orgId") Long orgId,
            @Param("objectId") String objectId);

    @Query("SELECT COUNT(e) FROM VREvent e WHERE e.session.id = :sessionId AND e.organisation.id = :orgId AND e.eventType = :eventType")
    long countEventsByType(
            @Param("sessionId") Long sessionId,
            @Param("orgId") Long orgId,
            @Param("eventType") String eventType);

    @Query("SELECT MAX(e.sequenceNumber) FROM VREvent e WHERE e.session.id = :sessionId AND e.organisation.id = :orgId")
    Optional<Integer> findMaxSequenceNumberBySessionIdAndOrganisationId(@Param("sessionId") Long sessionId, @Param("orgId") Long orgId);
}
