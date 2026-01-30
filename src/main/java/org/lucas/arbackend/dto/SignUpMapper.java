package org.lucas.arbackend.dto;

import org.lucas.arbackend.dto.helper.SignUpResponse;
import org.lucas.arbackend.entity.Organisation;
import org.lucas.arbackend.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class SignUpMapper {

    public SignUpResponse toResponse(Organisation org, Profile profile) {
        return SignUpResponse.builder()
                .orgId(org.getId())
                .orgName(profile.getOrgName())
                .email(org.getEmail())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .createAt(org.getCreatedAt())
                .endedAt(org.getEndedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
