package org.lucas.arbackend.repository.QAndA;

import org.lucas.arbackend.entity.QAndA.CourseQuestionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseQuestionReplyRepository extends JpaRepository<CourseQuestionReply, Long> {
}
