package io.mosip.authentication.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.authentication.service.dto.request.AuditLogRequestDTO;
import io.mosip.authentication.service.dto.response.AuditLogResponseDTO;
import io.mosip.authentication.service.service.AuditLogService;
import io.mosip.kernel.core.http.RequestWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for AuditController
 *
 * @author MOSIP
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class AuditLogControllerTest {

    @Mock
    private AuditLogService auditLogService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private AuditLogController auditLogController;

    private RequestWrapper<AuditLogRequestDTO> validRequestWrapper;
    private AuditLogResponseDTO mockAuditResponse;

    private static final String URL = "/idauthentication/v1/audit/log";

    @Before
    public void before() {

        // Setup valid audit request
        AuditLogRequestDTO validAuditRequest = AuditLogRequestDTO.builder()
                .eventType("LOGIN_ATTEMPT")
                .description("User login successful")
                .userId("12345")
                .build();

        validRequestWrapper = new RequestWrapper<>();
        validRequestWrapper.setId("audit.log");
        validRequestWrapper.setVersion("1.0");
        validRequestWrapper.setRequest(validAuditRequest);
        validRequestWrapper.setRequesttime(LocalDateTime.now());

        // Setup mock response
        mockAuditResponse = AuditLogResponseDTO.builder()
                .eventId("AUDIT-20231001120000-ABC123")
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .message("Event logged successfully")
                .build();

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLogAuditEvent_Success() throws Exception {
        // Arrange
        when(auditLogService.logEvent(any(AuditLogRequestDTO.class))).thenReturn(mockAuditResponse);

        // Act & Assert
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validRequestWrapper)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("audit.log"))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.response.eventId").value("AUDIT-20231001120000-ABC123"))
                .andExpect(jsonPath("$.response.message").value("Event logged successfully"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    /**
     * Test audit event logging with missing eventType
     */
    @Test
    public void testLogAuditEvent_MissingEventType() throws Exception {
        // Arrange - build request missing eventType
        AuditLogRequestDTO invalidRequest = AuditLogRequestDTO.builder()
                .description("User login successful")
                .userId("12345")
                .build();
        // eventType is null - should fail validation

        RequestWrapper<AuditLogRequestDTO> invalidRequestWrapper = new RequestWrapper<>();
        invalidRequestWrapper.setId("audit.log");
        invalidRequestWrapper.setVersion("1.0");
        invalidRequestWrapper.setRequest(invalidRequest);
        invalidRequestWrapper.setRequesttime(LocalDateTime.now());

        // Act & Assert - expect HTTP 400 due to @Valid constraint violation
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidRequestWrapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].errorCode").value("IDA-VAL-001"))
                .andExpect(jsonPath("$.errors[0].message").value(containsString("eventType")));
    }

    /**
     * Test audit event logging with empty userId
     */
    @Test
    public void testLogAuditEvent_EmptyUserId() throws Exception {
        // Arrange - build request with empty userId
        AuditLogRequestDTO invalidRequest = AuditLogRequestDTO.builder()
                .eventType("LOGIN_ATTEMPT")
                .description("User login successful")
                .userId("")
                .build();

        RequestWrapper<AuditLogRequestDTO> invalidRequestWrapper = new RequestWrapper<>();
        invalidRequestWrapper.setId("audit.log");
        invalidRequestWrapper.setVersion("1.0");
        invalidRequestWrapper.setRequest(invalidRequest);
        invalidRequestWrapper.setRequesttime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidRequestWrapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].errorCode").value("IDA-VAL-001"))
                .andExpect(jsonPath("$.errors[0].message").value(containsString("userId")));
    }

    /**
     * Test audit event logging when service throws exception
     */
    @Test
    public void testLogAuditEvent_ServiceException() throws Exception {
        // Arrange
        when(auditLogService.logEvent(any(AuditLogRequestDTO.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validRequestWrapper)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("Failed to log audit event")));
    }
}
