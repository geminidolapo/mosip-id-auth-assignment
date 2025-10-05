package io.mosip.authentication.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mosip.id.auth.health")
@Data
public class HealthProperties {

    private String config;

    private String status;

    private String serviceName;

    private String version;

    private String environment;
}
