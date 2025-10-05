package io.mosip.authentication.service.service;


import io.mosip.authentication.service.dto.request.AuditLogRequestDTO;
import io.mosip.authentication.service.dto.response.AuditLogResponseDTO;
import io.mosip.authentication.service.model.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Audit Service Implementation using in-memory storage
 *
 * @author MOSIP
 */
@Slf4j
@Service
public class AuditLogService {

    private final List<AuditEvent> auditEvents = new CopyOnWriteArrayList<>();

    // Additional index for faster user-based lookups
    private final Map<String, List<AuditEvent>> userEventsMap = new ConcurrentHashMap<>();

    private static final String EVENT_ID_PREFIX = "AUDIT-";
    private static final int MAX_EVENTS = 10000;

    @PostConstruct
    public void init() {
        log.info("AuditService initialized with in-memory storage");
    }

    public AuditLogResponseDTO logEvent(AuditLogRequestDTO auditRequest) {

        String eventId = generateEventId();

        AuditEvent auditEvent = AuditEvent.builder()
                .eventId(eventId)
                .userId(auditRequest.getUserId())
                .eventType(auditRequest.getEventType())
                .description(auditRequest.getDescription())
                .build();

        storeEvent(auditEvent);

        log.info("Audit event logged: {} for user: {}", auditRequest.getEventType(), auditRequest.getUserId());
        return AuditLogResponseDTO.builder()
                .eventId(eventId)
                .status("SUCCESS")
                .timestamp(auditEvent.getTimestamp())
                .message("Event logged successfully")
                .build();
    }

    private void storeEvent(AuditEvent event) {
        synchronized (this) {
            // Check capacity and remove the oldest event if needed
            if (auditEvents.size() >= MAX_EVENTS) {
                AuditEvent oldestEvent = auditEvents.remove(0);
                removeFromUserIndex(oldestEvent);
                log.warn("Maximum event capacity reached, removed oldest event: {}", oldestEvent.getEventId());
            }
        }
        auditEvents.add(event);
        addToUserIndex(event);
    }

    private void addToUserIndex(AuditEvent event) {
        userEventsMap.computeIfAbsent(event.getUserId(), k -> new CopyOnWriteArrayList<>())
                .add(event);
    }

    private void removeFromUserIndex(AuditEvent event) {
        List<AuditEvent> userEvents = userEventsMap.get(event.getUserId());
        if (userEvents != null) {
            userEvents.removeIf(e -> e.getEventId().equals(event.getEventId()));
            if (userEvents.isEmpty()) {
                userEventsMap.remove(event.getUserId());
            }
        }
    }

    private String generateEventId() {
        return EVENT_ID_PREFIX +
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}