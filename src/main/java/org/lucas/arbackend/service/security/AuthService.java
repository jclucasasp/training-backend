package org.lucas.arbackend.service.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.service.messaging.CustomEmailType;
import org.lucas.arbackend.service.messaging.EmailProducer;
import org.lucas.arbackend.util.OTPService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final EmailProducer emailProducer;
    private final OrganisationRepository orgRepo;
    private final StaffRepository staffRepo;
    private final StudentRepository studentRepo;
    private final OTPService otpService;

    public void sendResetPasswordEmail(String email) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String otp = otpService.generateOtp(6);

        Optional<Organisation> org = orgRepo.findByEmail(email);
        if (org.isPresent()) {
            emailProducer.queueEmail(org.get().getProfile().getOrgName(), org.get().getEmail(), otp, CustomEmailType.RESET);
            return;
        }

        Optional<Staff> staff = staffRepo.findByEmail(email);
        if (staff.isPresent()) {
            String fullName = String.join(" ", staff.get().getFirstName(), staff.get().getLastName());
            emailProducer.queueEmail(fullName, staff.get().getEmail(), otp, CustomEmailType.RESET);
            return;
        }

        Optional<Student> student = studentRepo.findByEmail(email);
        if (student.isPresent()) {
            String fullName = String.join(" ", student.get().getFirstName(), student.get().getLastName());
            emailProducer.queueEmail(fullName, student.get().getEmail(), otp, CustomEmailType.RESET);
            return;
        }

        throw new EntityNotFoundException("No account found...");

    }
}
