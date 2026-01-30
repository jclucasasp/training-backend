package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.lucas.arbackend.dto.OrganisationMapper;
import org.lucas.arbackend.dto.SignUpMapper;
import org.lucas.arbackend.dto.helper.OrganisationRequest;
import org.lucas.arbackend.dto.helper.OrganisationResponse;
import org.lucas.arbackend.dto.helper.SignUpRequest;
import org.lucas.arbackend.dto.helper.SignUpResponse;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.entity.Profile;
import org.lucas.arbackend.repository.OrganisationRepository;
import org.lucas.arbackend.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class OrganisationService {

    private final OrganisationRepository orgRepo;
    private final ProfileRepository profileRepo;
    private final OrganisationMapper orgMapper;
    private final SignUpMapper signUpMapper;

    // SingUp Create
    public SignUpResponse signUp(SignUpRequest request) {
        Organisation org = Organisation.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        Profile profile = Profile.builder()
                .organisation(org) // Establishes the link for @MapsId
                .orgName(request.getOrgName())
                .vatNumber(request.getVatNumber())
                .registrationNumber(request.getRegistrationNumber())
                .build();

        // Set the profile on the organisation (bidirectional link)
        org.setProfile(profile);

        // Save only the Organisation.
        // CascadeType.ALL will automatically save the Profile as well.
        Organisation savedOrg = orgRepo.save(org);

        // Note: savedOrg.getProfile() will now contain the saved profile data
        return signUpMapper.toResponse(savedOrg, savedOrg.getProfile());
    }

    // READ (All)
    public List<OrganisationResponse> findAll() {
        return orgRepo.findAll().stream()
                .map(orgMapper::toResponse)
                .collect(Collectors.toList());
    }

    // READ (Single)
    public OrganisationResponse findById(Long id) {
        return orgRepo.findById(id)
                .map(orgMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with id: " + id));
    }

    // UPDATE
    public OrganisationResponse update(Long id, OrganisationRequest details) {
        Organisation existing = orgRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        existing.setEmail(details.getEmail());
        if (details.getPassword() != null) {
            existing.setPassword(details.getPassword());
        }

        return orgMapper.toResponse(orgRepo.save(existing));
    }

    // DELETE
    public void delete(Long id) {
        if (!orgRepo.existsById(id)) {
            throw new EntityNotFoundException("Organisation not found");
        }
        orgRepo.deleteById(id);
    }

}