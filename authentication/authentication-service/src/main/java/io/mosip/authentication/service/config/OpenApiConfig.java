package io.mosip.authentication.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ID Authentication Service API")
                        .version("1.2.0")
                        .description("MOSIP ID Authentication Service for authentication and E-KYC operations")
                        .contact(new Contact()
                                .name("MOSIP Support")
                                .email("info@mosip.io")
                                .url("https://docs.mosip.io"))
                        .license(new License()
                                .name("Mozilla Public License 2.0")
                                .url("https://www.mozilla.org/en-US/MPL/2.0/"))
                        .termsOfService("https://docs.mosip.io/terms"));
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .pathsToMatch("/idauthentication/v1/auth/**", "/idauthentication/v1/kyc/**")
                .packagesToScan("io.mosip.authentication.service.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi healthAuditApi() {
        return GroupedOpenApi.builder()
                .group("health-audit")
                .pathsToMatch("/idauthentication/v1/health/**", "/idauthentication/v1/audit/**")
                .packagesToScan("io.mosip.authentication.service.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("actuator")
                .pathsToMatch("/idauthentication/actuator/**")
                .build();
    }
}
