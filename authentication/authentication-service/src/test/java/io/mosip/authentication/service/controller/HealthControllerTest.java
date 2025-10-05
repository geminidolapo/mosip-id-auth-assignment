package io.mosip.authentication.service.controller;

import io.mosip.authentication.service.dto.response.HealthDetailsResponseDTO;
import io.mosip.authentication.service.service.HealthDetailsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for HealthController
 *
 * @author MOSIP
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class HealthControllerTest {

    @Mock
    private HealthDetailsService healthDetailsService;

    @InjectMocks
    private HealthDetailsController healthDetailsController;

    @Autowired
    private MockMvc mockMvc;

    private HealthDetailsResponseDTO mockHealthResponse;

    private static final String URL = "/idauthentication/v1/health/details";

    @Before
    public void before() {
        // Set up a mock health response
        mockHealthResponse = HealthDetailsResponseDTO.builder()
                .status("UP")
                .serviceName("id-authentication-service")
                .version("1.2.0")
                .environment("dev")
                .configurableProperty("HEALTH_CHECK")
                .build();

        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test a successful health check with UP status
     */
    @Test
    public void testGetHealthDetails_Success() throws Exception {
        // Arrange
        when(healthDetailsService.getHealthStatus()).thenReturn(mockHealthResponse);

        // Act & Assert
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ida.health.details"))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.response.status").value("UP"))
                .andExpect(jsonPath("$.response.serviceName").value("id-authentication-service"))
                .andExpect(jsonPath("$.response.version").value("1.2.0"))
                .andExpect(jsonPath("$.response.environment").value("dev"))
                .andExpect(jsonPath("$.response.configurableProperty").value("HEALTH_CHECK"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    /**
     * Test health check with DOWN status from service
     */
    @Test
    public void testGetHealthDetails_ServiceReturnsDown() throws Exception {
        // Arrange
        HealthDetailsResponseDTO downResponse = HealthDetailsResponseDTO.builder()
                .status("DOWN")
                .timestamp(LocalDateTime.now())
                .serviceName("id-authentication-service")
                .version("1.2.0")
                .environment("dev")
                .configurableProperty("HEALTH_CHECK")
                .build();

        when(healthDetailsService.getHealthStatus()).thenReturn(downResponse);

        // Act & Assert
        mockMvc.perform(get(URL))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.id").value("ida.health.details"))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.response.status").value("DOWN"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    /**
     * Test health check when service throws exception
     */
    @Test
    public void testGetHealthDetails_ServiceException() throws Exception {
        // Arrange
        when(healthDetailsService.getHealthStatus())
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get(URL))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.id").value("ida.health.details"))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.response").isEmpty())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("Health check failed")));
    }

    /**
     * Test health check with different environments
     */
    @Test
    public void testGetHealthDetails_DifferentEnvironments() throws Exception {
        // Arrange
        String[] environments = {"dev", "test", "staging", "production"};

        for (String env : environments) {
            HealthDetailsResponseDTO envResponse = HealthDetailsResponseDTO.builder()
                    .status("UP")
                    .timestamp(LocalDateTime.now())
                    .serviceName("id-authentication-service")
                    .version("1.2.0")
                    .environment(env)
                    .configurableProperty(env + "-value")
                    .build();

            when(healthDetailsService.getHealthStatus()).thenReturn(envResponse);

            // Act & Assert
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response.environment").value(env))
                    .andExpect(jsonPath("$.response.configurableProperty").value(env + "-value"));
        }
    }

    /**
     * Test health check with different service versions
     */
    @Test
    public void testGetHealthDetails_DifferentVersions() throws Exception {
        // Arrange
        String[] versions = {"1.0.0", "1.1.0", "1.2.0", "2.0.0"};

        for (String version : versions) {
            HealthDetailsResponseDTO versionResponse = HealthDetailsResponseDTO.builder()
                    .status("UP")
                    .timestamp(LocalDateTime.now())
                    .serviceName("id-authentication-service")
                    .version(version)
                    .environment("production")
                    .configurableProperty("config-value")
                    .build();

            when(healthDetailsService.getHealthStatus()).thenReturn(versionResponse);

            // Act & Assert
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response.version").value(version));
        }
    }

    /**
     * Test health check with partial service failure
     */
    @Test
    public void testGetHealthDetails_PartialServiceFailure() throws Exception {
        // Arrange
        HealthDetailsResponseDTO partialResponse = HealthDetailsResponseDTO.builder()
                .status("DEGRADED")
                .timestamp(LocalDateTime.now())
                .serviceName("id-authentication-service")
                .version("1.2.0")
                .environment("production")
                .configurableProperty("degraded-mode")
                .build();

        when(healthDetailsService.getHealthStatus()).thenReturn(partialResponse);

        // Act & Assert - DEGRADED status should still return 200 OK
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("DEGRADED"));
    }

    /**
     * Test health check endpoint accessibility
     */
    @Test
    public void testGetHealthDetails_EndpointAccessibility() throws Exception {
        // Act & Assert - Test various HTTP methods should not be allowed
        mockMvc.perform(post(URL))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put(URL))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete(URL))
                .andExpect(status().isMethodNotAllowed());

        // GET should be allowed
        when(healthDetailsService.getHealthStatus()).thenReturn(mockHealthResponse);
        mockMvc.perform(get(URL))
                .andExpect(status().isOk());
    }
}