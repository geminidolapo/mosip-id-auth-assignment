package io.mosip.authentication.service.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * Audit Request DTO
 *
 * @author MOSIP
 */
@Data
@Builder
@Schema(description = "Audit Event Request")
public class AuditLogRequestDTO {

    @NotBlank(message = "eventType is mandatory")
    @Schema(description = "Type of event", example = "LOGIN", required = true)
    private String eventType;

    @Schema(description = "Event description", example = "User attempted login")
    private String description;

    @NotBlank(message = "userId is mandatory")
    @Schema(description = "User ID associated with the event", example = "12345", required = true)
    private String userId;
}