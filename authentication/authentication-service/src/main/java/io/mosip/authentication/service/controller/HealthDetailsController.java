package io.mosip.authentication.service.controller;


import io.mosip.authentication.service.constant.HealthStatus;
import io.mosip.authentication.service.dto.response.HealthDetailsResponseDTO;
import io.mosip.authentication.service.service.HealthDetailsService;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Health Controller for service health monitoring and metadata
 *
 * @author MOSIP
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health Monitoring", description = "Endpoints for service health monitoring")
public class HealthDetailsController {

    /** The Constant HEALTH_CONTROLLER. */
    private static final String HEALTH_CONTROLLER = "HealthDetailsController";

    /** The Constant GET_HEALTH_DETAILS. */
    private static final String GET_HEALTH_DETAILS = "getHealthDetails";

    private final HealthDetailsService healthDetailsService;

    /**
     * Get service health details.
     *
     * @return the response entity with health details
     */
    @PreAuthorize("hasAnyRole('INDIVIDUAL','PARTNER','MISP','RESIDENT')")
    @GetMapping(value = "/details")
    @Operation(summary = "Get service health details",
            description = "Returns service health status, metadata and configurable properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "503", description = "Service is unavailable",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    public ResponseEntity<ResponseWrapper<HealthDetailsResponseDTO>> getHealthDetails() {
        HealthDetailsResponseDTO healthResponse = healthDetailsService.getHealthStatus();

        ResponseWrapper<HealthDetailsResponseDTO> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId("ida.health.details");
        responseWrapper.setVersion("1.0");
        responseWrapper.setResponse(healthResponse);
        responseWrapper.setErrors(null);
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());

        if (HealthStatus.DOWN.name().equals(healthResponse.getStatus())) {
            log.warn("{}::{} - Health check status DOWN: service={} env={}",
                    HEALTH_CONTROLLER, GET_HEALTH_DETAILS,
                    healthResponse.getServiceName(), healthResponse.getEnvironment());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseWrapper);
        }

        log.debug("{}::{} - Health check status UP: service={} env={}",
                HEALTH_CONTROLLER, GET_HEALTH_DETAILS,
                healthResponse.getServiceName(), healthResponse.getEnvironment());
        return ResponseEntity.ok(responseWrapper);
    }
}
