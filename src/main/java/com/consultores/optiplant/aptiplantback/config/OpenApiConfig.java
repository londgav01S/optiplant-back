package com.consultores.optiplant.aptiplantback.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI para la documentación de la API REST, incluyendo información general y configuración de seguridad con JWT Bearer.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura la instancia de OpenAPI con información general de la API y esquema de seguridad para autenticación JWT Bearer.
     * @return
     */
    @Bean
    public OpenAPI openApi() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Aptiplant API")
                .description("API backend para gestion de inventario multi-sucursal")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components().addSecuritySchemes(
                securitySchemeName,
                new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ));
    }
}

