package io.mosip.authentication.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Health Response DTO
 *
 * @author MOSIP
 */
@Data
@Builder
@Schema(description = "Health Response containing service status and metadata")
public class HealthDetailsResponseDTO {

    @Schema(description = "Service status (UP/DOWN)", example = "UP")
    private String status;

    @Schema(description = "Timestamp of health check", example = "2023-10-01T12:00:00", format = "date-time",
            type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC", shape = JsonFormat.Shape.STRING)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Service name", example = "id-authentication-service")
    private String serviceName;

    @Schema(description = "Service version", example = "1.0.0")
    private String version;

    @Schema(description = "Deployment environment", example = "dev")
    private String environment;

    @Schema(description = "Configurable property from application", example = "true")
    private String configurableProperty;
}
