package org.lucas.arbackend.service;

import org.lucas.arbackend.dto.helper.OrganisationRequest;
import org.lucas.arbackend.dto.helper.OrganisationResponse;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.repository.OrganisationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganisationService {

    private final OrganisationRepository repository;

    public OrganisationService(OrganisationRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public OrganisationResponse create(OrganisationRequest request) {
        Organisation newOrg = new Organisation();
        newOrg.setEmail(request.getEmail());
        newOrg.setPassword(request.getPassword());
        repository.save(newOrg);
        return mapToDto(newOrg);
    }

    // READ (All)
    public List<OrganisationResponse> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // READ (Single)
    public OrganisationResponse findById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with id: " + id));
    }

    // UPDATE
    public OrganisationResponse update(Long id, OrganisationRequest details) {
        Organisation existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        existing.setEmail(details.getEmail());
        if (details.getPassword() != null) {
            existing.setPassword(details.getPassword());
        }

        return mapToDto(repository.save(existing));
    }

    // DELETE
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Organisation not found");
        }
        repository.deleteById(id);
    }

    private OrganisationResponse mapToDto(Organisation org) {
        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .createdAt(org.getCreatedAt())
                .endedAt(org.getEndedAt())
                .passwordResetDate(org.getPasswordResetDate())
                .build();
    }
}