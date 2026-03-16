package org.lucas.arbackend.repository.QAndA;

import org.lucas.arbackend.entity.QAndA.CourseQuestionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseQuestionReplyRepository extends JpaRepository<CourseQuestionReply, Long> {
    Optional<CourseQuestionReply> findByIdAndOrganisationId(Long replyId, Long orgId);

    @Modifying
    @Query("UPDATE CourseQuestionReply r SET r.isAcceptedAnswer = false " +
            "WHERE r.question.id = :questionId AND r.organisation.id = :orgId")
    void clearAcceptedAnswerForQuestion(@Param("questionId") Long questionId, @Param("orgId") Long orgId);
}
