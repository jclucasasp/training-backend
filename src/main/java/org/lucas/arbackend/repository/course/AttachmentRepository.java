package org.lucas.arbackend.repository.course;

import org.lucas.arbackend.entity.course.misc.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
