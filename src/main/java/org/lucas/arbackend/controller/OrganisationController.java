package org.lucas.arbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.lucas.arbackend.dto.helper.OrganisationRequest;
import org.lucas.arbackend.dto.helper.OrganisationResponse;
import org.lucas.arbackend.dto.helper.SignUpRequest;
import org.lucas.arbackend.dto.helper.SignUpResponse;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.entity.Profile;
import org.lucas.arbackend.repository.OrganisationRepository;
import org.lucas.arbackend.repository.ProfileRepository;
import org.lucas.arbackend.service.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("api/v1/organisations")
public class OrganisationController {

    private final OrganisationRepository orgRepo;
    private final ProfileRepository profileRepo;
    private final OrganisationService service;

    @PostMapping
    public ResponseEntity<SignUpResponse> create(@RequestBody @Validated SignUpRequest request) {

        log.info("Creating new organisation: {}", request);

        Organisation org = Organisation.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        Organisation savedOrg = orgRepo.save(org);

        Profile profile = Profile.builder()
                .orgName(request.getOrgName())
                .registrationNumber(request.getRegistrationNumber())
                .vatNumber(request.getVatNumber())
                .build();
        Profile savedProfile = profileRepo.save(profile);

        return ResponseEntity.ok(SignUpResponse.builder()
                .orgId(savedOrg.getId())
                .orgName(savedProfile.getOrgName())
                .registrationNumber(savedProfile.getRegistrationNumber())
                .vatNumber(savedProfile.getVatNumber())
                .createAt(savedOrg.getCreatedAt())
                .updatedAt(savedOrg.getUpdatedAt())
                .endedAt(savedOrg.getEndedAt())
                .build());
    }

    @GetMapping
    public List<OrganisationResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganisationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganisationResponse> update(@PathVariable Long id, @RequestBody @Validated OrganisationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
