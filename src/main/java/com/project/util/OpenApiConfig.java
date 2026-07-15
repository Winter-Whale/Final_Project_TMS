package com.project.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Parking API")
                        .version("1.0.0")
                        .description("Сервис для бронирования парковочных мест.\n" +
                                "Возможные роли: **ADMIN**, **OWNER** (владелец места), **RENTER** (арендатор).\n" +
                                "Для доступа к защищённым эндпоинтам используйте JWT-токен, полученный при аутентификации.")
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .schemaRequirement("BearerAuth", new SecurityScheme()
                        .name("BearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                );
    }
}
