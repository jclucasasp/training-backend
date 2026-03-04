package org.lucas.arbackend.repository.QAndA;

import org.lucas.arbackend.entity.QAndA.CourseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, Long> {
    Optional<CourseQuestion> findByIdAndOrganisationId(Long questionId, Long aLong);

    Collection<CourseQuestion> findAllByCourseIdAndOrganisationId(Long courseId, Long aLong);
}
