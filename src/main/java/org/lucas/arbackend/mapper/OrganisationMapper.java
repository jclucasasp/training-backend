package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.organisation.OrganisationRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.entity.Organisation.OrgAddress;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganisationMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateOrganisation(OrganisationRequest dto, @MappingTarget Organisation entity);

    // 2. Update the Profile entity
    @Mapping(target = "orgId", ignore = true)
    void updateProfile(OrganisationRequest dto, @MappingTarget Profile entity);

    // 3. Update the Address entity
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "zip", source = "zip") // Explicit if names match, but good for clarity
    void updateAddress(OrganisationRequest dto, @MappingTarget OrgAddress entity);

    // 4. Return the OrganisationResponse entity

    default OrganisationResponse mapToOrgResponse(Organisation org) {
        return mapToOrgResponse(org, null);
    }

    @Mapping(target = "orgName", source = "org.profile.orgName")
    @Mapping(target = "registrationNumber", source = "org.profile.registrationNumber")
    @Mapping(target = "vatNumber", source = "org.profile.vatNumber")

    @Mapping(target = "streetAddress", source = "org.profile.address.street")
    @Mapping(target = "suburb", source = "org.profile.address.suburb")
    @Mapping(target = "city", source = "org.profile.address.city")
    @Mapping(target = "state", source = "org.profile.address.state")
    @Mapping(target = "zip", source = "org.profile.address.zip")
    @Mapping(target = "apiKey", expression = "java(rawKey != null ? rawKey : (org.getApiKey() != null ? org.getApiKey().getHashKey() : null))")
//    @Mapping(target = "subscriptionStatus", expression = "java(org.getSubscription().getStatus() == 1)")
    @Mapping(target = "subscriptionStatus", source = "org.subscription.status")
    @Mapping(target = "subscriptionPlan", source = "org.subscription.subscriptionPlan.plan")
    @Mapping(target = "subscriptionAmount", source = "org.subscription.subscriptionAmount")
    @Mapping(target = "subscriptionStartDate", source = "org.subscription.createdAt")
    @Mapping(target = "subscriptionUpdatedDate", source = "org.subscription.updatedAt")
    @Mapping(target = "subscriptionEndDate", source = "org.subscription.endedAt")
    OrganisationResponse mapToOrgResponse(Organisation org, String rawKey);

    // 5. Return the Organisation entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Handle encoding in service
    Organisation mapToOrganisation(OrganisationRequest dto);

    default String mapSubscriptionStatus(Integer status) {
        if (status == null) return "NULL";
        if (Integer.valueOf(1).equals(status)) return "ACTIVE";

        return "INACTIVE";
    }

}
