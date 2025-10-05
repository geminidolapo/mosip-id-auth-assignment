package io.mosip.authentication.service.service;

import io.mosip.authentication.service.config.HealthProperties;
import io.mosip.authentication.service.dto.response.HealthDetailsResponseDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class HealthDetailsService {

    private final HealthProperties properties;

    public HealthDetailsService(@Qualifier("mosip.id.auth.health-io.mosip.authentication.service.config.HealthProperties")
                         HealthProperties properties) {
        this.properties = properties;
    }

    public HealthDetailsResponseDTO getHealthStatus() {
        return HealthDetailsResponseDTO.builder()
                .status(properties.getStatus())
                .serviceName(properties.getServiceName())
                .version(properties.getVersion())
                .environment(properties.getEnvironment())
                .configurableProperty(properties.getConfig())
                .build();
    }
}
