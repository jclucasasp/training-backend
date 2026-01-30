package org.lucas.arbackend.controller;

import org.lucas.arbackend.dto.helper.OrganisationRequest;
import org.lucas.arbackend.dto.helper.OrganisationResponse;
import org.lucas.arbackend.dto.helper.SignUpRequest;
import org.lucas.arbackend.dto.helper.SignUpResponse;
import org.lucas.arbackend.service.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/organisations")
public class OrganisationController {

    private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SignUpResponse> create(@RequestBody SignUpRequest request) {
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
    public ResponseEntity<OrganisationResponse> update(@PathVariable Long id, @RequestBody OrganisationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
