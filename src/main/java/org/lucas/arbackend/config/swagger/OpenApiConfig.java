package org.lucas.arbackend.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// TODO: Update the url once live.
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
                version = "1.0",
                license = @License(
                        name = "Proprietary",
                        url = "https://your-website.com/license"
                )
        ),
        // Applies security globally to all paths by default (optional)
        security = {
                @SecurityRequirement(name = "X-API-KEY"),
                @SecurityRequirement(name = "BasicAuth")
        }
)
@SecurityScheme(
        name = "X-API-KEY",
        description = "API auth description for students",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER
)
@SecurityScheme(
        name = "BasicAuth",
        description = "Normal login session for staff members",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class OpenApiConfig {
}
