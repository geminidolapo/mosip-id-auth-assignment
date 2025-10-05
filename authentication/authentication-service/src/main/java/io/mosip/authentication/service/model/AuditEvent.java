package io.mosip.authentication.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Audit Event Entity
 *
 * @author MOSIP
 */
@Data
@Builder
@Schema(description = "Audit Event Entity")
public class AuditEvent {

    @Schema(description = "Unique event ID", example = "AUDIT-ABC123")
    private String eventId;

    @Schema(description = "Type of event", example = "LOGIN")
    private String eventType;

    @Schema(description = "Event description", example = "User attempted login")
    private String description;

    @Schema(description = "User ID associated with the event", example = "12345")
    private String userId;

    @Schema(description = "Event timestamp", example = "2023-10-01T12:00:00")
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}
