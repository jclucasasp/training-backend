package org.lucas.arbackend.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrganisationNotFoundException extends RuntimeException{
    private final String msg;
}
