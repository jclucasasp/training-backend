package org.lucas.arbackend.repository.QAndA;

import org.lucas.arbackend.entity.QAndA.CourseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, Long> {
    Optional<CourseQuestion> findByIdAndOrganisationId(Long questionId, Long aLong);

    List<CourseQuestion> findAllByCourseIdAndOrganisationId(Long courseId, Long orgId);
}
