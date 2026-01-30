package org.lucas.arbackend.dto;

import lombok.*;
import org.lucas.arbackend.dto.helper.OrganisationRequest;
import org.lucas.arbackend.dto.helper.OrganisationResponse;
import org.lucas.arbackend.entity.Organisation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrganisationMapper {

    private final PasswordEncoder encoder;

    public Organisation toEntity(OrganisationRequest request) {
        if (request == null) {
            return null;
        }

        return Organisation.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .build();
    }

    public OrganisationResponse toResponse(Organisation organisation) {
        if (organisation == null) {
            return null;
        }

        return OrganisationResponse.builder()
                .id(organisation.getId())
                .email(organisation.getEmail())
                .createdAt(organisation.getCreatedAt())
                .endedAt(organisation.getEndedAt())
                .passwordResetDate(organisation.getPasswordResetDate())
                .build();
    }

}
