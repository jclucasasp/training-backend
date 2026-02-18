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
    @Mapping(target = "orgName", source = "profile.orgName")
    @Mapping(target = "registrationNumber", source = "profile.registrationNumber")
    @Mapping(target = "vatNumber", source = "profile.vatNumber")
    @Mapping(target = "streetAddress", source = "profile.address.street")
    @Mapping(target = "suburb", source = "profile.address.suburb")
    @Mapping(target = "city", source = "profile.address.city")
    @Mapping(target = "state", source = "profile.address.state")
    @Mapping(target = "zip", source = "profile.address.zip")
    @Mapping(target = "apiKey", source = "apiKey.hashKey")
    @Mapping(target = "subscriptionStatus", expression = "java(org.getSubscription().getStatus() == 1)")
    @Mapping(target = "subscriptionPlan", source = "subscription.subscriptionPlan.plan")
    @Mapping(target = "subscriptionStartDate", source = "subscription.createdAt")
    @Mapping(target = "subscriptionEndDate", source = "subscription.endedAt")
    OrganisationResponse mapToOrgResponse(Organisation org);

    // 5. Return the Organisation entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Handle encoding in service
    Organisation mapToOrganisation(OrganisationRequest dto);
}
