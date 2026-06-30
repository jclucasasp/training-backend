package org.lucas.arbackend.service.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.OTPService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AuthLookupService findByEmail;
    private final OTPService otpService;
    private final StudentRepository studentRepo;
    private final OrganisationRepository orgRepo;
    private final StaffRepository staffRepo;
    private final CacheService cacheService;

    public void sendOtp(String email) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        CacheDto entityFound = findByEmail.getAuthCacheDto(email);

        String fullName = String.join(" ", entityFound.getFirstName(), entityFound.getLastName());
        otpService.otpTimer(email, fullName);
//        emailProducer.queueEmail(fullName, entityFound.getEmail(), otp, CustomEmailType.RESET);
    }

    public void changePassword(String email, String otp, String newPassword) {
        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            throw new IllegalArgumentException("Email, OTP and new password cannot be empty");
        }

        CacheDto entityFound = findByEmail.getAuthCacheDto(email);
        log.info("DEBUG: Incoming otp [{}]", otp);
        String otpFound = otpService.getOtpHashMap(email);
        log.info("DEBUG: Found otp [{}]", otpFound);

        if (otpService.getOtpHashMap(entityFound.getEmail()) == null || !otpService.getOtpHashMap(entityFound.getEmail()).equals(otp)) {
            throw new IllegalArgumentException("OTP either expired or is not valid!");
        }

        Long entityId = entityFound.getId();
        String encodedPassword = passwordEncoder.encode(newPassword);
        RoleTypes role;
        try {
            role = RoleTypes.valueOf(entityFound.getRoleName());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unknown or missing role type: " + entityFound.getRoleName());
        }

        switch (role) {
            case STUDENT -> {
                Student student = studentRepo.findById(entityId)
                        .orElseThrow(() -> new EntityNotFoundException("Student not found"));
                student.setPassword(encodedPassword);
                studentRepo.save(student);
            }
            case ORG_ADMIN -> {
                Organisation org = orgRepo.findById(entityId)
                        .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
                org.setPassword(encodedPassword);
                orgRepo.save(org);
            }
            case COURSE_EDITOR, SUPPORT -> {
                Staff staff = staffRepo.findById(entityId)
                        .orElseThrow(() -> new EntityNotFoundException("Staff not found"));
                staff.setPassword(encodedPassword);
                staffRepo.save(staff);
            }
        }

        cacheService.evictAuthUser(email);
    }
}
