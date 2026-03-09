package org.lucas.arbackend.mapper;

import org.lucas.arbackend.dto.organisation.StaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.security.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StaffMapper {

    @Mapping(target = "role", source = "role.name")
    StaffResponse maptoStaffResponse(Staff entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateStaff(StaffRequest dto, @MappingTarget Staff entity);

    @Mapping(target = "id", ignore = true)
    void updateRole(StaffRequest dto, @MappingTarget Role entity);

    default String mapRoleToString(Role role) {
        if (role == null) {
            return null;
        }

        return role.getName();
    }
}
