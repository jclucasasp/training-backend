package org.lucas.arbackend.mapper.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;

@Getter @RequiredArgsConstructor
public class MappingContext {
    private final Organisation organisation;
    private final Student student;
    private final Staff staff;
}
