package io.mosip.authentication.service.controller;

import io.mosip.authentication.service.dto.request.AuditLogRequestDTO;
import io.mosip.authentication.service.dto.response.AuditLogResponseDTO;
import io.mosip.authentication.service.service.AuditLogService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

/**
 * Audit Controller for event logging using in-memory storage
 *
 * @author MOSIP
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/audit")//has been set in context-path
@Tag(name = "audit-controller", description = "Audit Event Logging Controller")
public class AuditLogController {

    /** The Constant AUDIT_LOG_CONTROLLER. */
    private static final String AUDIT_LOG_CONTROLLER = "AuditLogController";

    /** The Constant LOG_AUDIT_EVENT. */
    private static final String LOG_AUDIT_EVENT = "logAuditEvent";

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyRole('INDIVIDUAL','PARTNER','MISP','RESIDENT','ADMIN')")
    @PostMapping(value = "/log")
    @Operation(summary = "Log audit event", description = "Logs an audit event in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Audit event logged successfully",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    public ResponseEntity<ResponseWrapper<AuditLogResponseDTO>> logAuditEvent(
            @Valid @RequestBody RequestWrapper<AuditLogRequestDTO> auditRequestDTO) {

        AuditLogResponseDTO response = auditLogService.logEvent(auditRequestDTO.getRequest());

        ResponseWrapper<AuditLogResponseDTO> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(auditRequestDTO.getId());
        responseWrapper.setVersion(auditRequestDTO.getVersion());
        responseWrapper.setResponse(response);
        responseWrapper.setErrors(null);
        responseWrapper.setResponsetime(DateUtils.getUTCCurrentDateTime());

        return new ResponseEntity<>(responseWrapper, HttpStatus.CREATED);
    }
}
