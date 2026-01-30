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

       return ResponseEntity.ok(service.signUp(request));
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
