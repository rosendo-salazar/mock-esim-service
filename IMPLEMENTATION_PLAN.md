# Mock eSIM Service - Implementation Plan

## Executive Summary

This document outlines the comprehensive plan to create a mock Maya Mobile API service for end-to-end testing of the fly-roamy-api application. The mock service will replicate the Maya Connectivity API endpoints, allowing seamless environment switching between the mock (development/staging) and production (real Maya API).

**Target Deployment**: Azure App Service (matching fly-roamy-api)
**Authentication**: Basic Auth (API Key:Secret)

---

## 1. Research Findings

### 1.1 Maya API Endpoints (Based on fly-roamy-api Integration)

From analyzing `MayaMobileService.java` and `FulfillmentService.java`:

| Operation | HTTP Method | Endpoint Pattern | Purpose |
|-----------|-------------|------------------|---------|
| Provision eSIM | POST | `/connectivity/esims` | Create new eSIM with QR code |
| Attach Plan | POST | `/connectivity/esims/{esimId}/plans` | Attach data plan to eSIM |
| Get eSIM Details | GET | `/connectivity/esims/{esimId}` | Retrieve eSIM status and data |
| Deactivate eSIM | DELETE | `/connectivity/esims/{esimId}` | Deactivate an eSIM |

### 1.2 NEW: Plans/Bundles API (For Database Sync)

Based on Maya's Connect+ Platform, these endpoints are needed to sync plans:

| Operation | HTTP Method | Endpoint Pattern | Purpose |
|-----------|-------------|------------------|---------|
| List All Plans | GET | `/connectivity/bundles` | Get all available data bundles |
| Get Plan Details | GET | `/connectivity/bundles/{bundleId}` | Get specific bundle details |
| Get Plans by Country | GET | `/connectivity/bundles?country={iso}` | Filter bundles by coverage |
| Get Plans by Region | GET | `/connectivity/bundles?region={region}` | Filter bundles by region |

### 1.3 Existing Data Models

**Plan Model** (from `fly-roamy-api/model/Plan.java`):
- `id`, `productId`, `name`, `data` (GB), `days`, `price`
- `currencyPrices` (Map of currency -> price)
- `packageType` (country/region/global)
- `countries` (List of ISO codes)
- `provider`, `providerId` - **For Maya integration**
- `wholesaleCost`, `margin` - For profit tracking
- `isActive`, `badge`, `availableFrom`, `availableTo`

**ESIM Model** (from `fly-roamy-api/model/ESIM.java`):
- `orderIds` - **Already supports multiple orders/top-offs**
- `topOffCount`, `lastTopOffDate` - Top-off tracking
- `remainingDataMB`, `dataUsedMB` - Data tracking
- `mayaEsimId`, `iccid`, `matchingId` - Maya identifiers

### 1.4 Admin Panel Integration Points

**fly-roamy-admin** (Angular 17.3.0):
- Plan management via `/admin/plans` endpoints
- Client-side caching with `shareReplay(1)`
- Bulk operations (create, update, delete)
- Excel bulk upload for plans
- Location-based filtering (countries/regions)

---

## 2. Technical Standards (Matching fly-roamy-api)

### 2.1 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.x |
| Build Tool | Gradle | 8.x |
| Database | MongoDB | (for state persistence) |
| Testing | JUnit 5 + Mockito | Latest |
| API Docs | OpenAPI/Swagger | 3.0 |
| Code Coverage | JaCoCo | 70% minimum |
| Deployment | Azure App Service | Linux |

### 2.2 Project Structure

```
mock-esim-service/
├── src/
│   ├── main/
│   │   ├── java/com/flyroamy/mock/
│   │   │   ├── MockEsimServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── MongoConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── EsimController.java
│   │   │   │   ├── BundleController.java      # Plans/Bundles API
│   │   │   │   ├── UsageController.java
│   │   │   │   ├── WebhookController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── ProvisionEsimRequest.java
│   │   │   │   │   ├── AttachPlanRequest.java
│   │   │   │   │   └── SimulateUsageRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── EsimResponse.java
│   │   │   │       ├── EsimDetailsResponse.java
│   │   │   │       ├── BundleResponse.java    # Plan/Bundle response
│   │   │   │       ├── BundleListResponse.java
│   │   │   │       ├── AttachPlanResponse.java
│   │   │   │       └── ErrorResponse.java
│   │   │   ├── model/
│   │   │   │   ├── MockEsim.java
│   │   │   │   ├── MockBundle.java            # Maya's term for Plan
│   │   │   │   ├── AttachedPlan.java          # Plan attached to eSIM
│   │   │   │   ├── MockUsageRecord.java
│   │   │   │   └── WebhookSubscription.java
│   │   │   ├── repository/
│   │   │   │   ├── MockEsimRepository.java
│   │   │   │   ├── MockBundleRepository.java
│   │   │   │   └── WebhookSubscriptionRepository.java
│   │   │   ├── service/
│   │   │   │   ├── EsimService.java
│   │   │   │   ├── BundleService.java         # Plan management
│   │   │   │   ├── QrCodeService.java
│   │   │   │   ├── UsageSimulatorService.java
│   │   │   │   ├── WebhookService.java
│   │   │   │   └── DataSeederService.java
│   │   │   ├── security/
│   │   │   │   └── BasicAuthFilter.java
│   │   │   ├── exception/
│   │   │   │   ├── EsimNotFoundException.java
│   │   │   │   ├── BundleNotFoundException.java
│   │   │   │   ├── InvalidRequestException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── util/
│   │   │       ├── IccidGenerator.java
│   │   │       └── MatchingIdGenerator.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-azure.properties
│   │       └── data/
│   │           └── seed-bundles.json
│   └── test/
│       └── java/com/flyroamy/mock/
│           ├── controller/
│           ├── service/
│           └── integration/
├── postman/
│   └── Maya_Mock_API_Collection.json
├── build.gradle
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 3. API Specification

### 3.1 Authentication

**Basic Authentication** (matching Maya):
```http
Authorization: Basic base64(apiKey:apiSecret)
```

Example:
```bash
# API Key: maya_test_key, Secret: maya_test_secret
Authorization: Basic bWF5YV90ZXN0X2tleTptYXlhX3Rlc3Rfc2VjcmV0
```

### 3.2 eSIM Endpoints

#### 3.2.1 Provision eSIM

**POST** `/v1/connectivity/esims`

Request:
```json
{
  "bundleId": "bundle_usa_5gb_30d",
  "userEmail": "user@example.com",
  "profileType": "consumer",
  "metadata": {
    "orderId": "order_456",
    "userId": "user_123"
  }
}
```

Response (201 Created):
```json
{
  "esimId": "maya_abc12345",
  "iccid": "89012345678901234567",
  "matchingId": "uuid-matching-id",
  "qrCodeUrl": "https://mock-maya.azurewebsites.net/qr/maya_abc12345",
  "qrCodeData": "LPA:1$smdp.example.com$ACTIVATION_CODE",
  "activationCode": "ABCD1234EFGH5678",
  "status": "provisioned",
  "createdAt": "2025-12-26T10:00:00Z"
}
```

#### 3.2.2 Attach Plan to eSIM (Supports Multiple Plans)

**POST** `/v1/connectivity/esims/{esimId}/bundles`

This endpoint allows attaching **additional plans** to an existing eSIM (top-off).

Request:
```json
{
  "bundleId": "bundle_usa_10gb_30d",
  "activationType": "immediate",
  "stackData": true
}
```

Response (200 OK):
```json
{
  "esimId": "maya_abc12345",
  "bundleId": "bundle_usa_10gb_30d",
  "status": "attached",
  "activationDate": "2025-12-26T10:00:00Z",
  "expiryDate": "2026-01-25T10:00:00Z",
  "dataAllowanceMB": 10240,
  "totalRemainingDataMB": 15360,
  "attachedBundles": [
    {
      "bundleId": "bundle_usa_5gb_30d",
      "attachedAt": "2025-12-20T10:00:00Z",
      "dataAllowanceMB": 5120,
      "remainingDataMB": 5120,
      "status": "active"
    },
    {
      "bundleId": "bundle_usa_10gb_30d",
      "attachedAt": "2025-12-26T10:00:00Z",
      "dataAllowanceMB": 10240,
      "remainingDataMB": 10240,
      "status": "active"
    }
  ]
}
```

#### 3.2.3 Get eSIM Details (With All Attached Plans)

**GET** `/v1/connectivity/esims/{esimId}`

Response (200 OK):
```json
{
  "esimId": "maya_abc12345",
  "iccid": "89012345678901234567",
  "matchingId": "uuid-matching-id",
  "status": "active",
  "activationDate": "2025-12-20T10:00:00Z",
  "totalDataAllowanceMB": 15360,
  "totalDataUsedMB": 512,
  "totalRemainingDataMB": 14848,
  "lastUsed": "2025-12-26T15:30:00Z",
  "attachedBundles": [
    {
      "bundleId": "bundle_usa_5gb_30d",
      "bundleName": "USA 5GB 30 Days",
      "attachedAt": "2025-12-20T10:00:00Z",
      "expiryDate": "2026-01-19T10:00:00Z",
      "dataAllowanceMB": 5120,
      "dataUsedMB": 512,
      "remainingDataMB": 4608,
      "status": "active",
      "countries": ["us"],
      "packageType": "country"
    },
    {
      "bundleId": "bundle_usa_10gb_30d",
      "bundleName": "USA 10GB 30 Days",
      "attachedAt": "2025-12-26T10:00:00Z",
      "expiryDate": "2026-01-25T10:00:00Z",
      "dataAllowanceMB": 10240,
      "dataUsedMB": 0,
      "remainingDataMB": 10240,
      "status": "active",
      "countries": ["us"],
      "packageType": "country"
    }
  ],
  "network": {
    "mcc": "310",
    "mnc": "260",
    "operator": "T-Mobile"
  }
}
```

#### 3.2.4 Deactivate eSIM

**DELETE** `/v1/connectivity/esims/{esimId}`

Response (200 OK):
```json
{
  "esimId": "maya_abc12345",
  "status": "deactivated",
  "deactivationDate": "2025-12-26T16:00:00Z"
}
```

#### 3.2.5 List eSIMs

**GET** `/v1/connectivity/esims`

Query Parameters:
- `status` - Filter by status
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)

### 3.3 Bundles/Plans Endpoints (NEW - For Database Sync)

These endpoints allow fly-roamy-api to fetch available plans from Maya and sync to the local database.

#### 3.3.1 List All Bundles

**GET** `/v1/connectivity/bundles`

Query Parameters:
- `country` - Filter by country ISO code (e.g., `us`, `mx`)
- `region` - Filter by region (e.g., `europe`, `asia`, `latam`)
- `packageType` - Filter by type (`country`, `region`, `global`)
- `page` - Page number (default: 0)
- `size` - Page size (default: 100)

Response (200 OK):
```json
{
  "bundles": [
    {
      "bundleId": "bundle_usa_5gb_30d",
      "productId": 12345,
      "name": "USA 5GB 30 Days",
      "description": "5GB data valid for 30 days in the United States",
      "dataGB": 5.0,
      "validityDays": 30,
      "price": {
        "amount": 15.00,
        "currency": "USD"
      },
      "prices": {
        "USD": 15.00,
        "EUR": 13.50,
        "MXN": 270.00,
        "CAD": 20.00,
        "GBP": 12.00,
        "AUD": 23.00
      },
      "wholesaleCost": 8.50,
      "packageType": "country",
      "countries": ["us"],
      "coverage": {
        "networks": [
          {"mcc": "310", "mnc": "260", "operator": "T-Mobile", "technology": "5G"},
          {"mcc": "311", "mnc": "480", "operator": "Verizon", "technology": "5G"},
          {"mcc": "310", "mnc": "410", "operator": "AT&T", "technology": "5G"}
        ]
      },
      "isActive": true,
      "badge": "Most Popular",
      "unlimitedType": null,
      "createdAt": "2025-01-01T00:00:00Z",
      "updatedAt": "2025-12-26T00:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 100,
    "totalElements": 250,
    "totalPages": 3
  }
}
```

#### 3.3.2 Get Bundle Details

**GET** `/v1/connectivity/bundles/{bundleId}`

Response (200 OK):
```json
{
  "bundleId": "bundle_usa_5gb_30d",
  "productId": 12345,
  "name": "USA 5GB 30 Days",
  "description": "5GB data valid for 30 days in the United States",
  "dataGB": 5.0,
  "validityDays": 30,
  "price": {
    "amount": 15.00,
    "currency": "USD"
  },
  "prices": {
    "USD": 15.00,
    "EUR": 13.50,
    "MXN": 270.00
  },
  "wholesaleCost": 8.50,
  "packageType": "country",
  "countries": ["us"],
  "coverage": {
    "networks": [...]
  },
  "isActive": true,
  "terms": "Data valid for 30 days from activation. No voice/SMS.",
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-12-26T00:00:00Z"
}
```

#### 3.3.3 Get Bundles by Country

**GET** `/v1/connectivity/bundles?country=us`

Returns all bundles that cover the specified country.

#### 3.3.4 Get Bundles by Region

**GET** `/v1/connectivity/bundles?region=europe`

Returns all bundles that cover the specified region.

---

## 4. Multi-Plan eSIM Support

### 4.1 How It Works

The mock service supports multiple plans attached to a single eSIM:

1. **Initial Provisioning**: Creates eSIM with first plan
2. **Top-Off**: Attaches additional plans via `/esims/{esimId}/bundles`
3. **Data Stacking**: When `stackData: true`, data from all plans combines
4. **Usage Tracking**: Each attached plan tracks its own usage
5. **Expiry**: Plans expire independently based on their validity

### 4.2 Data Model

```java
public class MockEsim {
    private String esimId;
    private String iccid;
    private String matchingId;
    private String status;
    private List<AttachedBundle> attachedBundles;  // Multiple plans
    private Integer totalDataUsedMB;
    private LocalDateTime createdAt;
}

public class AttachedBundle {
    private String bundleId;
    private String bundleName;
    private LocalDateTime attachedAt;
    private LocalDateTime expiryDate;
    private Integer dataAllowanceMB;
    private Integer dataUsedMB;
    private Integer remainingDataMB;
    private String status;  // active, expired, depleted
}
```

### 4.3 Integration with fly-roamy-api

The existing `ESIM.java` model already supports this via:
- `orderIds: List<String>` - Tracks multiple purchase orders
- `topOffCount: Integer` - Number of top-offs
- `lastTopOffDate: LocalDateTime` - Last top-off timestamp

**Recommendation**: Add a new field to track individual attached plans:
```java
@Field("attachedPlans")
private List<AttachedPlanInfo> attachedPlans;
```

---

## 5. Database Sync Feature

### 5.1 Sync Flow (fly-roamy-api to Local DB)

```
┌─────────────────┐     GET /bundles     ┌─────────────────┐
│  fly-roamy-api  │ ──────────────────▶  │  Mock Maya API  │
│   (or Admin)    │                      │                 │
└────────┬────────┘                      └─────────────────┘
         │
         │ Transform & Save
         ▼
┌─────────────────┐
│    MongoDB      │
│  master-plans   │
└─────────────────┘
```

### 5.2 Recommended Implementation

**New Endpoint in fly-roamy-api** (`/api/admin/plans/sync`):

```java
@PostMapping("/admin/plans/sync")
@RequireAdmin
public ResponseEntity<?> syncPlansFromMaya() {
    // 1. Fetch all bundles from Maya API
    List<MayaBundle> bundles = mayaMobileService.getAllBundles();

    // 2. Transform to local Plan format
    List<Plan> plans = bundles.stream()
        .map(this::transformToLocalPlan)
        .collect(Collectors.toList());

    // 3. Upsert to database (by providerId)
    int created = 0, updated = 0;
    for (Plan plan : plans) {
        if (planRepository.existsByProviderId(plan.getProviderId())) {
            planRepository.updateByProviderId(plan);
            updated++;
        } else {
            planRepository.save(plan);
            created++;
        }
    }

    return ResponseEntity.ok(Map.of(
        "success", true,
        "created", created,
        "updated", updated
    ));
}
```

### 5.3 Field Mapping

| Maya Bundle Field | fly-roamy Plan Field |
|-------------------|----------------------|
| bundleId | providerId |
| productId | productId |
| name | name |
| dataGB | data |
| validityDays | days |
| price.amount | price |
| prices | currencyPrices |
| wholesaleCost | wholesaleCost |
| packageType | packageType |
| countries | countries |
| isActive | isActive |
| - | provider = "maya" |

### 5.4 Admin Panel Integration

Add a "Sync from Maya" button to the Plans page:

```typescript
// admin-plan-api.service.ts
syncFromMaya(): Observable<SyncResult> {
  return this.http.post<SyncResult>(`${this.apiUrl}/sync`, {});
}
```

```html
<!-- plans.component.html -->
<button (click)="syncFromMaya()" class="btn btn-primary">
  Sync from Maya
</button>
```

---

## 6. Admin Panel Recommendations

### 6.1 Environment Configuration

Update `fly-roamy-admin/src/environments/`:

```typescript
// environment.ts (Development)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  agentApiUrl: 'http://localhost:8081/api',
  mayaMockUrl: 'http://localhost:8082/v1',  // Mock service
  useMockMaya: true
};

// environment.stage.ts (Staging)
export const environment = {
  production: false,
  apiUrl: 'https://api-stage.roamyhub.com/api',
  mayaMockUrl: 'https://mock-maya-stage.azurewebsites.net/v1',
  useMockMaya: true
};

// environment.prod.ts (Production)
export const environment = {
  production: true,
  apiUrl: 'https://api.roamyhub.com/api',
  mayaApiUrl: 'https://api.maya.net/v1',  // Real Maya
  useMockMaya: false
};
```

### 6.2 New Admin Features

1. **Maya Sync Dashboard** (new component):
   - Show last sync timestamp
   - Display sync statistics (new, updated, removed plans)
   - Manual sync trigger button
   - Auto-sync schedule configuration

2. **Plan Source Indicator**:
   - Show "Maya" badge on plans from Maya
   - Show "Manual" badge on manually created plans
   - Indicate if plan is out of sync with Maya

3. **Bulk Operations Enhancement**:
   - "Sync Selected" - Re-sync specific plans from Maya
   - "Mark as Manual" - Exclude from auto-sync

### 6.3 Plans Page Enhancements

```typescript
// Add to PlanService
interface PlanWithSource extends Plan {
  source: 'maya' | 'manual';
  lastSynced?: string;
  syncStatus: 'synced' | 'modified' | 'new' | 'removed';
}
```

---

## 7. Webhook Support (Future)

### 7.1 Why Webhooks?

Webhooks allow Maya to push real-time updates to fly-roamy-api:

| Event | Use Case |
|-------|----------|
| `esim.activated` | User installed eSIM, update status |
| `esim.data_depleted` | Send top-off reminder email |
| `esim.expired` | Update status, prompt renewal |
| `esim.suspended` | Notify user of issue |
| `bundle.updated` | Plan price/details changed |
| `usage.threshold` | 80%/90% data usage alerts |

### 7.2 Implementation (When Ready)

**Register Webhook**:
```json
POST /v1/webhooks
{
  "url": "https://api.roamyhub.com/webhooks/maya",
  "events": ["esim.activated", "esim.data_depleted", "esim.expired"],
  "secret": "webhook_signing_secret_123"
}
```

**Webhook Payload**:
```json
{
  "event": "esim.data_depleted",
  "timestamp": "2025-12-26T15:00:00Z",
  "data": {
    "esimId": "maya_abc12345",
    "iccid": "89012345678901234567",
    "bundleId": "bundle_usa_5gb_30d",
    "dataUsedMB": 5120,
    "remainingDataMB": 0
  },
  "signature": "sha256=abc123..."
}
```

**fly-roamy-api Handler**:
```java
@PostMapping("/webhooks/maya")
public ResponseEntity<?> handleMayaWebhook(
    @RequestHeader("X-Maya-Signature") String signature,
    @RequestBody String payload) {

    // Verify signature
    if (!webhookService.verifySignature(payload, signature)) {
        return ResponseEntity.status(401).build();
    }

    // Process event
    WebhookEvent event = objectMapper.readValue(payload, WebhookEvent.class);
    switch (event.getEvent()) {
        case "esim.data_depleted":
            emailService.sendTopOffReminder(event.getData().getEsimId());
            break;
        case "esim.expired":
            esimService.markExpired(event.getData().getEsimId());
            break;
        // ...
    }

    return ResponseEntity.ok().build();
}
```

### 7.3 Mock Service Webhook Testing

The mock service includes admin endpoints for triggering webhook events:

```json
POST /v1/admin/webhooks/trigger
{
  "event": "esim.data_depleted",
  "esimId": "maya_abc12345"
}
```

---

## 8. Error Handling

### 8.1 Error Response Format

```json
{
  "error": {
    "code": "ESIM_NOT_FOUND",
    "message": "eSIM with ID 'maya_xyz' was not found",
    "details": {
      "esimId": "maya_xyz"
    },
    "timestamp": "2025-12-26T10:00:00Z",
    "requestId": "req_abc123"
  }
}
```

### 8.2 Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `ESIM_NOT_FOUND` | 404 | eSIM does not exist |
| `BUNDLE_NOT_FOUND` | 404 | Bundle/Plan does not exist |
| `INVALID_REQUEST` | 400 | Request validation failed |
| `ESIM_ALREADY_ACTIVE` | 409 | eSIM is already active |
| `ESIM_EXPIRED` | 410 | eSIM has expired |
| `BUNDLE_NOT_COMPATIBLE` | 400 | Bundle doesn't match eSIM coverage |
| `INSUFFICIENT_DATA` | 402 | No data remaining |
| `AUTHENTICATION_FAILED` | 401 | Invalid API credentials |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## 9. Testing Features

### 9.1 Admin Simulation Endpoints

```
POST /v1/admin/simulate/usage
POST /v1/admin/simulate/status
POST /v1/admin/simulate/expiry
DELETE /v1/admin/reset
POST /v1/admin/seed
GET /v1/admin/health
```

### 9.2 Configurable Behaviors

```properties
# Simulate latency
mock.latency.enabled=true
mock.latency.min-ms=50
mock.latency.max-ms=200

# Simulate failures
mock.failure.rate=0.0
mock.failure.types=TIMEOUT,SERVER_ERROR

# Auto-expire eSIMs
mock.auto-expire.enabled=false
mock.auto-expire.seconds=300

# Data depletion simulation
mock.usage.auto-decrement.enabled=false
mock.usage.auto-decrement.mb-per-minute=10
```

---

## 10. Postman Collection Structure

```
Maya Mock API Collection
├── 01. Authentication
│   ├── Valid Credentials Test
│   └── Invalid Credentials Test
├── 02. Bundles (Plans)
│   ├── List All Bundles
│   ├── Get Bundle by ID
│   ├── Filter by Country
│   ├── Filter by Region
│   └── Filter by Package Type
├── 03. eSIM Provisioning
│   ├── Provision New eSIM
│   ├── Provision Duplicate (Idempotency)
│   └── Provision with Invalid Bundle
├── 04. eSIM Management
│   ├── Get eSIM Details
│   ├── Get eSIM by ICCID
│   ├── List All eSIMs
│   ├── List by Status
│   └── Deactivate eSIM
├── 05. Multi-Plan (Top-Off)
│   ├── Attach Additional Plan
│   ├── Attach Plan (Stack Data)
│   ├── Get eSIM with Multiple Plans
│   └── Attach Incompatible Plan
├── 06. Usage Simulation
│   ├── Simulate Data Usage
│   ├── Simulate to Depletion
│   └── Get Usage History
├── 07. Webhooks
│   ├── Register Webhook
│   ├── List Webhooks
│   ├── Trigger Test Event
│   └── Delete Webhook
├── 08. Admin Operations
│   ├── Seed Test Data
│   ├── Reset All Data
│   ├── Force Status Change
│   └── Health Check
└── 09. Error Scenarios
    ├── eSIM Not Found
    ├── Bundle Not Found
    ├── Invalid Request Body
    ├── Expired eSIM Access
    └── Rate Limit Test
```

### 10.1 Environment Variables

```json
{
  "mock_base_url": "http://localhost:8082",
  "api_key": "maya_test_key",
  "api_secret": "maya_test_secret",
  "test_esim_id": "",
  "test_bundle_id": "bundle_usa_5gb_30d"
}
```

---

## 11. Deployment (Azure App Service)

### 11.1 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 11.2 Azure Configuration

```yaml
# azure-pipelines.yml (excerpt)
variables:
  - name: MAYA_MOCK_MONGO_URI
    value: $(MONGO_CONNECTION_STRING)
  - name: MAYA_MOCK_API_KEY
    value: $(MAYA_TEST_API_KEY)
  - name: MAYA_MOCK_API_SECRET
    value: $(MAYA_TEST_API_SECRET)
```

### 11.3 Application Properties

```properties
# application.properties (shared MongoDB cluster, separate database)
# Uses same cluster as fly-roamy-api but different database: maya-mock-db
spring.data.mongodb.uri=mongodb+srv://rosendosalazar_db_user:${MONGO_PASSWORD:TzzQb5zAJejPL7Ii}@fly-roamy-stage.neofgpw.mongodb.net/maya-mock-db?retryWrites=true&w=majority&appName=fly-roamy-stage
spring.data.mongodb.auto-index-creation=true

server.port=8082

# Mock API Authentication
mock.auth.api-key=${MAYA_MOCK_API_KEY:maya_test_key}
mock.auth.api-secret=${MAYA_MOCK_API_SECRET:maya_test_secret}

# Logging
logging.level.com.flyroamy.mock=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG
```

```properties
# application-azure.properties (Azure App Service)
spring.data.mongodb.uri=mongodb+srv://rosendosalazar_db_user:${MONGO_PASSWORD}@fly-roamy-stage.neofgpw.mongodb.net/maya-mock-db?retryWrites=true&w=majority&appName=fly-roamy-stage
server.port=8080

mock.auth.api-key=${MAYA_MOCK_API_KEY}
mock.auth.api-secret=${MAYA_MOCK_API_SECRET}

logging.level.com.flyroamy.mock=INFO
```

### 11.4 MongoDB Collections

The mock service will create these collections in the `maya-mock-db` database:

| Collection | Purpose |
|------------|---------|
| `mock_esims` | Provisioned eSIMs with attached bundles |
| `mock_bundles` | Available plans/bundles (seeded from your production plans) |
| `webhook_subscriptions` | Registered webhook endpoints |
| `usage_records` | Usage simulation history |
| `audit_logs` | API call logs for debugging |

---

## 12. Implementation Phases

### Phase 1: Core Infrastructure (Day 1)
- [ ] Initialize Spring Boot project with Gradle
- [ ] Configure MongoDB connection
- [ ] Implement Basic Auth filter
- [ ] Set up OpenAPI/Swagger documentation
- [ ] Create base exception handling
- [ ] Implement health check endpoint

### Phase 2: Bundle/Plans API (Day 2)
- [ ] Implement MockBundle model and repository
- [ ] Create BundleController with CRUD
- [ ] Seed initial bundles from JSON
- [ ] Implement filtering (country, region, type)
- [ ] Add pagination support

### Phase 3: eSIM Core Endpoints (Day 2-3)
- [ ] Implement MockEsim model with multi-plan support
- [ ] POST `/connectivity/esims` - Provision
- [ ] GET `/connectivity/esims/{esimId}` - Details
- [ ] POST `/connectivity/esims/{esimId}/bundles` - Attach plan
- [ ] DELETE `/connectivity/esims/{esimId}` - Deactivate
- [ ] GET `/connectivity/esims` - List with pagination

### Phase 4: QR Code & Extended Features (Day 3-4)
- [ ] Implement QR code generation (ZXing)
- [ ] Add ICCID and matching ID generators
- [ ] Implement usage tracking
- [ ] Add webhook subscription system
- [ ] Create admin simulation endpoints

### Phase 5: Testing & Postman (Day 4-5)
- [ ] Unit tests (70%+ coverage)
- [ ] Integration tests
- [ ] Create complete Postman collection
- [ ] Add test scripts and assertions
- [ ] Document all test scenarios

### Phase 6: Documentation & Deployment (Day 5-6)
- [ ] Complete README with setup instructions
- [ ] Docker configuration
- [ ] Azure App Service deployment guide
- [ ] API documentation finalization
- [ ] fly-roamy-api integration guide

---

## 13. Success Criteria

- [ ] All Maya endpoints from `MayaMobileService.java` implemented
- [ ] Bundle/Plans API for database sync working
- [ ] Multiple plans per eSIM supported
- [ ] Postman collection with 40+ test cases
- [ ] fly-roamy-api can switch environments with config only
- [ ] Admin panel can trigger plan sync
- [ ] Code coverage >= 70%
- [ ] Azure App Service deployment working
- [ ] Documentation complete

---

**Document Version**: 2.0
**Created**: December 26, 2025
**Updated**: December 26, 2025
**Author**: Claude Code
**Status**: Ready for Implementation
