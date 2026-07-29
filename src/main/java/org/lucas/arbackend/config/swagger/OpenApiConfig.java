package org.lucas.arbackend.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PagedModel;

// TODO: Update the fileUrl once live.
@Configuration
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Lucas",
                        email = "jclucasasp@gmail.com",
                        url = "https://your-website.com"
                ),
                description = "OpenAPI documentation for the Multi-Tenant AR Backend",
                title = "AR Backend API",
                version = "1.4",
                license = @License(
                        name = "Proprietary",
                        url = "https://your-website.com/license"
                )
        ),
        // Applies security globally to all paths by default (optional)
        security = {
                @SecurityRequirement(name = "SessionAuth"),
                @SecurityRequirement(name = "X-API-KEY"),
        }
)
// 1. Define Cookie-based session for Staff/Admins
@SecurityScheme(
        name = "SessionAuth",
        description = "Authentication via JSESSIONID cookie after login",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "JSESSIONID" // The actual cookie fileName used by Spring/Redis
)
// 2. Define Header-based API Key for Students
@SecurityScheme(
        name = "X-API-KEY",
        description = "API auth learningObjectives for students",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
