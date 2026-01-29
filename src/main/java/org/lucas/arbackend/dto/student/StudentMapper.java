package org.lucas.arbackend.dto.student;

import lombok.*;
import org.lucas.arbackend.dto.student.helper.StudentRequest;
import org.lucas.arbackend.dto.student.helper.StudentResponse;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.entity.Organisation;
import org.springframework.stereotype.Component;

@Component // Standard for production to allow @Autowired
public class StudentMapper {

    public Student toEntity(StudentRequest request, Organisation organisation) {
        if (request == null) return null;

        return Student.builder()
                .studentNumber(request.getStudentNumber())
                .organisation(organisation)
                .build();
    }

    public StudentResponse toResponse(Student student) {
        if (student == null) return null;

        return StudentResponse.builder()
                .id(student.getId())
                .studentNumber(student.getStudentNumber())
                .organisationId(student.getOrganisation() != null ? student.getOrganisation().getId() : null)
                .createdAt(student.getCreatedAt())
                .endedAt(student.getEndedAt())
                .build();
    }
}
