package org.lucas.arbackend.service.student;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.StudentMapper;
import org.lucas.arbackend.dto.student.helper.StudentRequest;
import org.lucas.arbackend.dto.student.helper.StudentResponse;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.repository.OrganisationRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final OrganisationRepository organisationRepository;
    private final StudentMapper mapper;

    public StudentResponse createStudent(StudentRequest request) {
        Organisation org = organisationRepository.findById(request.getOrganisationId())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Student student = mapper.toEntity(request, org);
        return mapper.toResponse(studentRepository.save(student));
    }

    public StudentResponse getStudentById(String studentNumber) {
        return mapper.toResponse(studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found")));
    }

    public StudentResponse updateStudent(String studentNumber, StudentRequest request) {
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        return mapper.toResponse(studentRepository.save(mapper.toEntity(request, student.getOrganisation())));
    }

    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(mapper::toResponse).toList();
    }
}
