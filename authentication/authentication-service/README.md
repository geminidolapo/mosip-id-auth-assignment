# Authentication Service

## About
Authentication Service is used by Authentication/E-KYC Partners
* to authenticate an individual's UIN/VID using one or more authentication types.
* to request E-KYC for an individual's UIN/VID using one or more authentication types.

## Authentication types
Any combination of the supported [authentication types](https://docs.mosip.io/1.2.0/id-authentication#authentication-types) may be used.

## Modalities
* Refer [biometric modalities](https://docs.mosip.io/1.2.0/biometrics#modalities).
* Above authentication types can be allowed/disallowed/mandated by the [configuration](../../docs/configuration.md#allowed-authentication-types) and the [Authentication/E-KYC Partner's Policy](../../docs/configuration.md).

## Partner/MISP validation
* Below partner/MISP data are validated before processing the authentication request:
    1. MISP License Key
    2. Partner ID
    3. Partner API Key

## Endpoints

### Core Authentication Endpoints

* **Authentication:**

```
POST /idauthentication/v1/auth/{MISP-LicenseKey}/{Auth-Partner-ID}/{Partner-Api-Key}
```

* **E-KYC:**

```
POST /idauthentication/v1/kyc/{MISP-LicenseKey}/{Auth-Partner-ID}/{Partner-Api-Key}
```

### Health Monitoring & Audit Endpoints

#### Health Details API
Provides comprehensive service health information, application metadata, and configuration details for monitoring and operational visibility.

* **Health Details:**

```
GET /idauthentication/api/health/details
```

**Response (200 OK):**
```json
{
  "id": "ida.health.details",
  "version": "1.0",
  "response": {
    "status": "UP",
    "timestamp": "2025-09-30T10:15:30",
    "serviceName": "id-authentication-service",
    "version": "1.2.0",
    "environment": "production",
    "configurableProperty": "production-ready-value"
  },
  "errors": null,
  "responseTime": "2025-09-30T10:15:30.123Z"
}
```

**Response (503 Service Unavailable):**
```json
{
  "id": "ida.health.details",
  "version": "1.0",
  "response": {
    "status": "DOWN",
    "timestamp": "2025-09-30T10:15:30",
    "serviceName": "id-authentication-service",
    "version": "1.2.0",
    "environment": "production",
    "configurableProperty": "production-ready-value"
  },
  "errors": null,
  "responseTime": "2025-09-30T10:15:30.123Z"
}
```

**Features:**
- Real-time service status monitoring (UP/DOWN)
- Application metadata (service name, version, environment)
- Configurable property exposure from application.properties
- HTTP 503 response when service is down
- ISO 8601 formatted timestamps

**Configuration:**
```properties
# Simulate service down for testing
mosip.id.auth.health.status=DOWN

# Configurable property to expose
mosip.ida.health.config=your-custom-value
```

#### Audit/Event Logging API
Handles audit event logging for tracking user activities and system events within the ID Authentication Service.

* **Log Audit Event:**

```
POST /idauthentication/api/v1/audit/log
```

**Request Body:**
```json
{
  "id": "audit.log",
  "version": "1.0",
  "request": {
    "eventType": "LOGIN_ATTEMPT",
    "userId": "12345",
    "description": "User attempted login with biometric"
  },
  "requestTime": "2025-09-30T10:15:30.123Z"
}
```

**Response (201 Created):**
```json
{
  "id": "audit.log",
  "version": "1.0",
  "response": {
    "eventId": "AUDIT-20250930101530-ABC123",
    "timestamp": "2025-09-30T10:15:30",
    "status": "SUCCESS",
    "message": "Event logged successfully"
  },
  "errors": null,
  "responseTime": "2025-09-30T10:15:30.123Z"
}
```

**Response (400 Bad Request):**
```json
{
  "id": "audit.log",
  "version": "1.0",
  "response": null,
  "errors": [
    {
      "errorCode": "IDA-VAL-001",
      "message": "eventType: Event type cannot be blank or null"
    },
    {
      "errorCode": "IDA-VAL-001",
      "message": "userId: User ID cannot be blank or null"
    }
  ],
  "responseTime": "2025-09-30T10:15:30.123Z"
}
```

**Validation Rules:**
- `eventType`: Mandatory
- `userId`: Mandatory
- `description`: Optional

**Features:**
- In-memory storage with automatic capacity management (10,000 event limit)
- Thread-safe implementation using CopyOnWriteArrayList and ConcurrentHashMap
- Optimized user-based lookups with secondary indexing
- Unique event ID generation with timestamp and UUID components
- Automatic cleanup of oldest events when capacity is reached
- Validation with detailed error messages

**Important Notes:** 
- Audit logs are stored **in-memory** only and will be lost on application restart
- Maximum capacity: 10,000 events (oldest events are automatically removed when limit is reached)
- For production environments requiring persistent audit trails, migrate to PostgreSQL or another persistent database

### API Documentation

Interactive API documentation is available via Swagger UI:

```
http://localhost:8090/idauthentication/swagger-ui.html
```

OpenAPI JSON specification:

```
http://localhost:8090/idauthentication/api-docs
```

## Callbacks for WebSub
* Master data update callback - to process WebSub message sent from Master data service and clear master data cache, so that in next authentication the master data is re-cached

## Security & Authorization

### Role-Based Access Control
The new health and audit endpoints implement Spring Security with role-based access:

**Health Endpoint Roles:**
- `INDIVIDUAL`
- `PARTNER`
- `MISP`
- `RESIDENT`

**Audit Endpoint Roles:**
- `INDIVIDUAL`
- `PARTNER`
- `MISP`
- `RESIDENT`
- `ADMIN`

### Configuration
```properties
# Enable/disable security
mosip.ida.security.enabled=true
```

## Dependencies

### Existing Dependencies
* Kernel Notification Service: For sending notifications for Authentication Success/Failure
* Kernel Audit Service
* Keycloak service: To get authentication token for connecting to the above kernel services
* WebSub: For getting events for Credential data/Identity data/Partner data/Master data updates
* BioSDK HTTP service: For biometric authentication
* HSM: For retrieving encryption/decryption keys

### New Dependencies
* **Spring Boot Validation**: For request validation
* **Spring Boot Actuator**: For health monitoring endpoints

**Important Note:** The current implementation uses in-memory storage for audit logs, which means:
- ✅ Fast performance with zero external dependencies
- ✅ Fast performance with zero external dependencies
- ✅ Thread-safe operations
- ❌ Data is temporary and will be cleared when the application restarts
- ❌ Not suitable for production environments requiring persistent audit trails

## Configuration

### Application Properties

```properties
# Application Metadata
spring.application.name=id-authentication-service
application.version=1.2.0
mosip.ida.environment=production

# Health Configuration
mosip.ida.health.config=production-ready-value
mosip.id.auth.health.status=DOWN

# Audit Service Configuration
mosip.ida.audit.max-events=10000
mosip.ida.audit.event-id-prefix=AUDIT-

# Server Configuration
server.port=8090
server.servlet.context-path=/idauthentication

# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics
```

### In-Memory Storage Details
The audit service uses an optimized in-memory storage implementation:

#### Storage Structure:
- Primary storage: **CopyOnWriteArrayList<AuditEvent>** for thread-safe operations
- Secondary index: **ConcurrentHashMap<String, List<AuditEvent>>** for fast user-based lookups
- Maximum capacity: 10,000 events (configurable)

#### Event ID Format:
- **AUDIT-{YYYYMMDDHHMMSS}-{RANDOM_UUID}**
- Example: AUDIT-20250930101530-ABC123**

#### Automatic Cleanup:
- When the 10,000 event limit is reached, the oldest event is automatically removed
- Both primary storage and user index are synchronized during cleanup
- Warning logs are generated when cleanup occurs

## Build and Deployment

### Build

```bash
# Clone the repository
git clone https://github.com/mosip/id-authentication.git
cd id-authentication

# Build the project
mvn clean install

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

### Testing

#### Unit Tests
The implementation includes comprehensive unit tests covering:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=HealthControllerTest
mvn test -Dtest=AuditControllereTest
```

#### Manual Testing

```bash
# Test health endpoint
curl -X GET http://localhost:8090/idauthentication/api/health/details

# Test audit logging
curl -X POST http://localhost:8090/idauthentication/api/audit/log \
  -H "Content-Type: application/json" \
  -d '{
    "id": "audit.log",
    "version": "1.0",
    "request": {
      "eventType": "LOGIN_ATTEMPT",
      "userId": "user123",
      "description": "User login attempt with biometric"
    },
    "requestTime": "2025-09-30T10:15:30.123Z"
  }'

# Test validation error
curl -X POST http://localhost:8090/idauthentication/api/audit/log \
  -H "Content-Type: application/json" \
  -d '{
    "id": "audit.log",
    "version": "1.0",
    "request": {
      "eventType": "",
      "userId": "",
      "description": "Invalid request"
    },
    "requestTime": "2025-09-30T10:15:30.123Z"
  }'
  
# Test capacity limits (generate multiple events)
for i in {1..100}; do
  curl -X POST http://localhost:8090/idauthentication/api/audit/log \
    -H "Content-Type: application/json" \
    -d "{
      \"id\": \"audit.log\",
      \"version\": \"1.0\",
      \"request\": {
        \"eventType\": \"TEST_EVENT\",
        \"userId\": \"user$i\",
        \"description\": \"Test event $i\"
      },
      \"requestTime\": \"2025-09-30T10:15:30.123Z\"
    }"
done

# Test malformed JSON
curl -X POST http://localhost:8090/idauthentication/api/v1/audit/log \
  -H "Content-Type: application/json" \
  -d '{
    "id": "audit.log",
    "version": "1.0",
    "request": {
      "eventType": "LOGIN_ATTEMPT",
      "userId": "user123"
    }'
    # Missing closing braces to simulate malformed JSON
```

## Monitoring & Observability

### Health Checks
Monitor service health through multiple endpoints:

```bash
# Custom health details endpoint
curl http://localhost:8090/idauthentication/api/health/details

# Spring Actuator health endpoint
curl http://localhost:8090/idauthentication/actuator/health

# Application info
curl http://localhost:8090/idauthentication/actuator/info

# Metrics
curl http://localhost:8090/idauthentication/actuator/metrics
```

### Audit Service Monitoring

Memory Usage:
- Monitor heap memory for audit event storage
- Each audit event consumes approximately 200-500 bytes
- Maximum memory usage: ~5-10 MB for 10,000 events

```
http://localhost:8090/idauthentication/h2-console
```

**Log Patterns:**
- **AuditService initialized with in-memory storage** - Service startup
- **Audit event logged: {eventType} for user: {userId}** - Successful event logging
- **Maximum event capacity reached, removed oldest event: {eventId}** - Capacity cleanup

**JMX Metrics:**
``` java
// Available metrics (if exposed)
audit.events.count
audit.events.capacity.used
audit.events.capacity.max
audit.users.count
```

## Architecture & Design

### Code Structure

```
io.mosip.authentication.service/
├── controller/
│   ├── HealthDetailsController.java      # Health monitoring endpoint
│   └── AuditLogController.java           # Audit logging endpoint
├── service/
│   ├── HealthDetailsService.java         # Health check business logic
│   └── AuditLogService.java              # Audit persistence logic
├── dto/
│   ├── /request/
│   │   ├── HealthDetailsRequestDTO.java  # Health request structure
|   |   └── AuditLogRequestDTO.java       # Audit request with validation
│   ├── /response/
│   │   ├── AuditLogResponseDTO.java
│   │   └── HealthDetailsResponseDTO.java # Health response structure
├── model/
│   └── AuditEvent.java                   # Audit event model
├── config/
│   ├── HealthProperties.java             # Health config properties
│   └── OpenApiConfig.java                # Open Api configuration
└── exception/
    └── GlobalExceptionHandler.java       # Centralized error handling
```

### Design Principles
- **Thread Safety**: Clear separation between controller, service, and data access layers
- **Performance**: Optimized data structures for read/write operations
- **Memory Management**: Memory Management
- **Separation of Concerns**: Clear separation between controller, service, and storage layers 
- **Single Responsibility**: Each class has a single, well-defined purpose
- **RESTful Design**: HTTP methods and status codes used appropriately
- **Testing**: Comprehensive unit tests with mocking

## Performance Considerations

### Memory Usage
- Base Memory: ~2-3 MB for empty data structures
- Per Event: ~200-500 bytes depending on string lengths
- Maximum: ~5-10 MB for 10,000 events
- Index Overhead: ~50-100% additional memory for user index

### Operations Performance
- Event Logging: O(1) average case
- User Lookup: O(1) with secondary index
- Capacity Cleanup: O(1) for removal, O(n) for index maintenance
- Concurrent Access: Minimal locking with copy-on-write semantics 

### Configuration Tuning
```properties
# Adjust based on memory constraints and audit requirements
mosip.ida.audit.max-events=5000
```

## Troubleshooting

### Common Issues
**Issue**:  High memory usage
```properties
# Solution: Reduce maximum event capacity
mosip.ida.audit.max-events=5000
```

**Issue**: Event ID collisions
```properties
# Solution: Event IDs include timestamp and random UUID components
# Collisions are extremely unlikely
```

**Issue**: Concurrent modification errors
```bash
# Solution: Ensure thread-safe data structures are used (already implemented)
# The service uses CopyOnWriteArrayList and ConcurrentHashMap
```

**Issue**: 401 Unauthorized errors
```properties
# Solution: Configure security or disable for testing
mosip.ida.security.enabled=false
```

**Issue**: Performance degradation with many events
```bash
# Solution: Reduce capacity or implement pagination for queries
mosip.ida.audit.max-events=2000
```

## Log Analysis
### Normal Operation:
```text
INFO  - AuditService initialized with in-memory storage
INFO  - Audit event logged: LOGIN for user: user123
```
### Capacity Warnings:
```text
WARN  - Maximum event capacity reached, removed oldest event: AUDIT-20250930101530-ABC123
```
### Error Scenarios:
```text
ERROR - Failed to log audit event: Validation failed for user ID
```

## Migration to Persistent Storage
### When to Migrate
Consider migrating from in-memory to persistent storage when:
- Audit logs must survive application restarts
- Regulatory compliance requires long-term audit trails
- High-volume event logging exceeds memory constraints
- Advanced querying and reporting capabilities are needed

### Implementation Options
1. **Database Integration:** PostgreSQL, MySQL with JPA
2. **File-based Storage:** JSON/CSV files with rotation
3. **External Services:** Elasticsearch, Splunk for analytics
4. **Hybrid Approach:** Elasticsearch, Splunk for analytics

## Future Enhancements

### Planned Features
1. **Query API** - GET endpoints to retrieve audit events by user, time range, or event type
2. **Persistent Storage Integration** - Database backend with migration path
3. **Advanced Filtering** - Complex queries with pagination and sorting
4. **Export Capabilities** - Export Capabilities
5. **Real-time Streaming** - WebSocket/SSE for live audit event monitoring
6. **Analytics Dashboard** - CSV/Excel export functionality
7. **Retention Policies** - Automated cleanup based on age or size
8. **Compression** - Memory optimization for long-running instances

## License
This project is part of MOSIP and is licensed under the Mozilla Public License 2.0 (MPL 2.0).

## Support
For issues, questions, and contributions:
- **GitHub Issues**: [mosip/id-authentication/issues](https://github.com/mosip/id-authentication/issues)
- **MOSIP Community**: https://community.mosip.io
- **Documentation**: https://docs.mosip.io
- **Email**: info@mosip.io