# API Documentation

## Deployed Versions

- **API/Backend (Render):**
  - Base URL: [https://rate-limiter-backend.onrender.com/api](https://rate-limiter-backend.onrender.com/api)
  - Swagger UI: [https://rate-limiter-backend.onrender.com/swagger-ui](https://rate-limiter-backend.onrender.com/swagger-ui)
  - OpenAPI spec: [https://rate-limiter-backend.onrender.com/v3/api-docs](https://rate-limiter-backend.onrender.com/v3/api-docs)

- **Frontend (Render):**
  - [https://rate-limiter-frontend-z0qo.onrender.com](https://rate-limiter-frontend-z0qo.onrender.com)

> **Note:** Render uses serverless containers; if the service has been idle there might be a "cold start" delay up to **50 seconds** the first time you access the URL or API after a period of inactivity.

## Local Development Base URL
```
http://localhost:8080/api
```

## Interactive API Documentation (Swagger)

The API includes interactive Swagger documentation:

- **Swagger UI (development):** `http://localhost:8080/swagger-ui.html`
- **Swagger UI (production):** [https://rate-limiter-backend.onrender.com/swagger-ui](https://rate-limiter-backend.onrender.com/swagger-ui)
  - Interactive interface to explore and test all endpoints
  - Try-it-out functionality for each endpoint
  - View request/response schemas
  - See rate limit headers documentation

- **OpenAPI JSON (production):** [https://rate-limiter-backend.onrender.com/v3/api-docs](https://rate-limiter-backend.onrender.com/v3/api-docs)
  - Machine-readable API specification
  - Can be imported into Postman, Insomnia, or other API clients

**Note:** Swagger endpoints are excluded from rate limiting for easy access.

## Example Test Client

For testing purposes, use the following example client:

```json
{
  "id": 1,
  "clientId": "test-client-001",
  "name": "Test Client",
  "description": "Example client for testing rate limiting functionality",
  "contactEmail": "testclient@example.com"
}
```

**Client ID for Testing**: `test-client-001`

## Client Management Endpoints

### Create Client
```http
POST /api/clients
Content-Type: application/json

{
  "clientId": "test-client-001",
  "name": "Test Client",
  "description": "Example client for testing rate limiting functionality",
  "contactEmail": "test-client@example.com"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "clientId": "test-client-001",
  "name": "Test Client",
  "description": "Example client for testing rate limiting functionality",
  "contactEmail": "test-client@example.com"
}
```

**Error Response (409 Conflict):**
```json
{
  "message": "Client ID already exists: test-client-001",
  "status": 409
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-01-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/clients"
}
```

### Get Client by ID
```http
GET /api/clients/{clientId}
```

**Example:**
```http
GET /api/clients/test-client-001
```

**Response (200 OK):**
```json
{
  "id": 1,
  "clientId": "test-client-001",
  "name": "Test Client",
  "description": "Example client for testing rate limiting functionality",
  "contactEmail": "test-client@example.com"
}
```

**Error Response (404 Not Found):**
```http
HTTP/1.1 404 Not Found
```

### Get All Clients
```http
GET /api/clients
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "clientId": "test-client-001",
    "name": "Test Client",
    "description": "Example client for testing rate limiting functionality",
    "contactEmail": "test-client@example.com"
  },
  {
    "id": 2,
    "clientId": "test-client-002",
    "name": "Test Client 2",
    "description": "Second test client",
    "contactEmail": "test-client-2@example.com"
  }
]
```

### Update Client
```http
PUT /api/clients/{clientId}
Content-Type: application/json

{
  "clientId": "test-client-001",
  "name": "Updated Test Client",
  "description": "Updated description",
  "contactEmail": "updated@example.com"
}
```

**Note**: The `clientId` field in the request body must match the path parameter. The client ID itself cannot be changed.

**Response (200 OK):**
```json
{
  "id": 1,
  "clientId": "test-client-001",
  "name": "Updated Test Client",
  "description": "Updated description",
  "contactEmail": "updated@example.com"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Client not found: test-client-001",
  "status": 404
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-01-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/clients/test-client-001"
}
```

### Delete Client
```http
DELETE /api/clients/{clientId}
```

**Example:**
```http
DELETE /api/clients/test-client-001
```

**Response (204 No Content):**
```
Empty response body
```

**Note**: Deleting a client will also delete any associated rate limit configs.

## Rate Limit Configuration Endpoints

### Create Rate Limit Configuration
```http
POST /api/rate-limits
Content-Type: application/json

{
  "clientId": "test-client-001",
  "timeWindowRequests": 100,
  "timeWindowSeconds": 60,
  "monthlyRequests": 10000,
  "throttlingMode": "HARD"
}
```

**Response:**
```json
{
  "id": 1,
  "clientId": "test-client-001",
  "timeWindowRequests": 100,
  "timeWindowSeconds": 60,
  "monthlyRequests": 10000,
  "throttlingMode": "HARD",
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00"
}
```

### Get Rate Limit Configuration
```http
GET /api/rate-limits/{clientId}
```

**Response:**
```json
{
  "id": 1,
  "clientId": "test-client-001",
  "timeWindowRequests": 100,
  "timeWindowSeconds": 60,
  "monthlyRequests": 10000,
  "throttlingMode": "HARD"
}
```

### Get All Rate Limit Configurations
```http
GET /api/rate-limits
```

**Response:**
```json
[
  {
    "id": 1,
    "clientId": "test-client-001",
    "timeWindowRequests": 100,
    "timeWindowSeconds": 60,
    "monthlyRequests": 10000,
    "throttlingMode": "HARD"
  }
]
```

### Update Rate Limit Configuration
```http
PUT /api/rate-limits/{clientId}
Content-Type: application/json

{
  "clientId": "test-client-001",
  "timeWindowRequests": 200,
  "timeWindowSeconds": 60,
  "monthlyRequests": 20000,
  "throttlingMode": "SOFT"
}
```

### Delete Rate Limit Configuration
```http
DELETE /api/rate-limits/{clientId}
```

## System Limit Configuration

### Get System Limits
```http
GET /api/system/limits
```

**Response:**
```json
{
  "id": 1,
  "globalRequestsPerSecond": 1000
}
```

### Update System Limits
```http
PUT /api/system/limits
Content-Type: application/json

{
  "globalRequestsPerSecond": 2000
}
```

## Notification Endpoints (Rate Limited)

### Send SMS Notification
```http
POST /api/notifications/sms
X-Client-Id: test-client-001
Content-Type: application/json

{
  "recipient": "+1234567890",
  "message": "Your verification code is 123456"
}
```

**Success Response (200):**
```json
{
  "status": "sent",
  "type": "SMS",
  "recipient": "+1234567890",
  "message": "SMS sent successfully"
}
```

**Rate Limit Exceeded (429):**
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1705312800
```

### Send Email Notification
```http
POST /api/notifications/email
X-Client-Id: test-client-001
Content-Type: application/json

{
  "recipient": "user@example.com",
  "message": "Welcome to our service!"
}
```

**Success Response (200):**
```json
{
  "status": "sent",
  "type": "EMAIL",
  "recipient": "user@example.com",
  "message": "Email sent successfully"
}
```

## Rate Limit Headers

All notification endpoints return rate limit information in response headers:

- `X-RateLimit-TimeWindow-Limit`: Maximum requests allowed in time window
- `X-RateLimit-TimeWindow-Remaining`: Remaining requests in time window
- `X-RateLimit-Monthly-Limit`: Maximum monthly requests
- `X-RateLimit-Monthly-Remaining`: Remaining monthly requests
- `X-RateLimit-Global-Limit`: Global system limit per second
- `X-RateLimit-Global-Remaining`: Remaining global requests
- `X-RateLimit-Warning`: Warning message (only in soft throttling mode)

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/rate-limits"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Rate limit config not found for client: client-123",
  "path": "/api/rate-limits/client-123"
}
```

### 429 Too Many Requests
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1705312800
```

## Testing with cURL

### Step 1: Create a test client
```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "test-client-001",
    "name": "Test Client",
    "description": "Example client for testing rate limiting functionality",
    "contactEmail": "test-client@example.com"
  }'
```

### Step 2: Create a rate limit configuration
```bash
curl -X POST http://localhost:8080/api/rate-limits \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "test-client",
    "timeWindowRequests": 5,
    "timeWindowSeconds": 60,
    "monthlyRequests": 1000,
    "throttlingMode": "HARD"
  }'
```

### Step 3: Send a notification (within limit)
```bash
curl -X POST http://localhost:8080/api/notifications/email \
  -H "X-Client-Id: test-client-001" \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "message": "Test message"
  }'
```

### Step 4: Test rate limiting (send multiple requests)
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/notifications/email \
    -H "X-Client-Id: test-client-001" \
    -H "Content-Type: application/json" \
    -d '{
      "recipient": "test@example.com",
      "message": "Test message '$i'"
    }' \
    -w "\nStatus: %{http_code}\n"
  echo "---"
done
```

