package org.lucas.arbackend.exception.handler;

import java.util.Map;

public record OrganisationErrorResponse(
        Map<String, String> errors
) {
}
