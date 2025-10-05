package io.mosip.authentication.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Audit Response DTO
 *
 * @author MOSIP
 */
@Data
@Builder
@Schema(description = "Audit Event Response")
public class AuditLogResponseDTO {

    @Schema(description = "Unique event ID", example = "AUDIT-12345", type = "string")
    private String eventId;

    @Schema(description = "Event timestamp", example = "2023-10-01T12:00:00", format = "date-time", type = "string")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private LocalDateTime timestamp;

    @Schema(description = "Event status", example = "SUCCESS", type = "string")
    private String status;

    @Schema(description = "Status message", example = "Event logged successfully")
    private String message;
}
