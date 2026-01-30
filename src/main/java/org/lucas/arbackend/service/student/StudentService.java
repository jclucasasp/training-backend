package org.lucas.arbackend.service.student;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.StudentMapper;
import org.lucas.arbackend.dto.student.helper.StudentRequest;
import org.lucas.arbackend.dto.student.helper.StudentResponse;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.entity.relationship.OrgApiRel;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.repository.OrganisationRepository;
import org.lucas.arbackend.repository.relationship.OrgApiRelRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepo;
    private final OrgApiRelRepository apiRelRepo;
    private final StudentMapper mapper;

    public List<StudentResponse> getAllByOrg(Long orgId) {
        return studentRepo.findAllByOrganisationId(orgId).stream()
                .map(mapper::toResponse).toList();
    }

    public void deleteStudent (Long studentId, Long orgId) {
        Student student = studentRepo.findByIdAndOrganisationId(studentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        studentRepo.delete(student);
    }

    public StudentResponse getOrCreateStudentAccess(String apiKey, String studentNumber) {
        // Resolve API Key to Organisation
        OrgApiRel rel = apiRelRepo.findByApiKeyValue(apiKey)
                .orElseThrow(() -> new BadCredentialsException("Invalid API Key"));

        Organisation org = rel.getOrganisation();

        // Multi-tenant Find or Create
        Student student = studentRepo.findByStudentNumberAndOrganisationId(studentNumber, org.getId())
                .orElseGet(() -> studentRepo.save(Student.builder()
                        .studentNumber(studentNumber)
                        .organisation(org)
                        .build()));

        return mapper.toResponse(student);
    }
}
