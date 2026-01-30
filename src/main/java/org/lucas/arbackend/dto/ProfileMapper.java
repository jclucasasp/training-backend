package org.lucas.arbackend.dto;

import org.lucas.arbackend.dto.helper.ProfileRequest;
import org.lucas.arbackend.dto.helper.ProfileResponse;
import org.lucas.arbackend.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileRequest toEntity(ProfileRequest profile) {
        if (profile == null) {
            return null;
        }

        return ProfileRequest.builder()
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .build();
    }

    public ProfileResponse toResponse(Profile profile) {
        if (profile == null) {
            return null;
        }

        return ProfileResponse.builder()
                .orgId(profile.getOrgId())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
