package com.mastercom.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class OpenApiConfig {
	 private final static String BEARER = "bearer";
	    private final static String JWT = "JWT";

	    @Bean
	    public OpenAPI customOpenAPI() {
	        final String securitySchemeName = "BearerAuth";
	        return new OpenAPI()
	                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
	                .components(
	                        new Components()
	                                .addSecuritySchemes(securitySchemeName,
	                                        new SecurityScheme()
	                                                .name(securitySchemeName)
	                                                .type(SecurityScheme.Type.HTTP)
	                                                .scheme(BEARER)
	                                                .bearerFormat(JWT)
	                                )
	                );
	    }
}
