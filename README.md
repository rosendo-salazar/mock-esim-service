# Mock eSIM Service

A mock implementation of the Maya Mobile Connectivity API for end-to-end testing of the fly-roamy-api application.

## Quick Start

### Prerequisites

- Java 21
- MongoDB (same cluster as fly-roamy-api)
- Gradle 8.x (wrapper included)

### Running Locally

```bash
# Clone and navigate to directory
cd mock-esim-service

# Run the application
./gradlew bootRun
```

The service will start on `http://localhost:8082`

### Default Credentials

```
API Key: maya_test_key
API Secret: maya_test_secret
```

## API Endpoints

### Bundles (Plans)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/connectivity/bundles` | List all bundles |
| GET | `/v1/connectivity/bundles/{bundleId}` | Get bundle details |
| GET | `/v1/connectivity/bundles?country=us` | Filter by country |
| GET | `/v1/connectivity/bundles?region=europe` | Filter by region |

### eSIMs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/connectivity/esims` | Provision new eSIM |
| GET | `/v1/connectivity/esims/{esimId}` | Get eSIM details |
| GET | `/v1/connectivity/esims` | List all eSIMs |
| POST | `/v1/connectivity/esims/{esimId}/bundles` | Attach bundle (top-off) |
| DELETE | `/v1/connectivity/esims/{esimId}` | Deactivate eSIM |

### Admin/Testing

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/admin/health` | Health check (public) |
| POST | `/v1/admin/simulate/usage` | Simulate data usage |
| POST | `/v1/admin/simulate/status` | Force status change |
| POST | `/v1/admin/seed` | Seed test bundles |
| DELETE | `/v1/admin/reset` | Reset all data |

### QR Codes (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/qr/{esimId}` | Get QR code image (PNG) |
| GET | `/qr/{esimId}/base64` | Get QR code as Base64 |

## Authentication

All endpoints (except health and QR codes) require Basic Authentication:

```bash
curl -u maya_test_key:maya_test_secret http://localhost:8082/v1/connectivity/bundles
```

## Swagger UI

API documentation is available at:
- http://localhost:8082/swagger-ui.html

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGO_PASSWORD` | (see props) | MongoDB password |
| `MAYA_MOCK_API_KEY` | maya_test_key | API key for authentication |
| `MAYA_MOCK_API_SECRET` | maya_test_secret | API secret for authentication |

### Application Properties

```properties
# MongoDB (uses same cluster as fly-roamy-api, different database)
spring.data.mongodb.uri=mongodb+srv://...@fly-roamy-stage.neofgpw.mongodb.net/maya-mock-db

# Server port
server.port=8082

# Authentication
mock.auth.api-key=maya_test_key
mock.auth.api-secret=maya_test_secret
```

## Integration with fly-roamy-api

Update `fly-roamy-api/src/main/resources/application.properties`:

```properties
# Development (use mock)
maya.api.base.url=http://localhost:8082/v1

# Production (use real Maya)
maya.api.base.url=https://api.maya.net/v1
```

## Postman Collection

Import the Postman collection from `postman/Maya_Mock_API_Collection.json`

## Development

### Build

```bash
./gradlew build
```

### Test

```bash
./gradlew test
```

### Docker

```bash
docker build -t mock-esim-service .
docker run -p 8082:8080 mock-esim-service
```

## License

Private - RoamyHub
