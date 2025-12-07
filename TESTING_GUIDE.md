# Testing Guide

## Prerequisites

1. Redis running on `localhost:6379`
2. Backend running on `http://localhost:8080`
3. Frontend running on `http://localhost:4200` (optional for manual testing)

## Running Tests

### Backend Tests

#### Unit Tests
```bash
cd backend
mvn test
```

#### Integration Tests
```bash
cd backend
mvn test
```

#### Test Coverage Report
```bash
cd backend
mvn test jacoco:report
```

View coverage report: Open `backend/target/site/jacoco/index.html` in a browser.

### Frontend Tests
```bash
cd frontend
npm test
```

## Manual Testing Scenarios

### Scenario 1: Time Window Rate Limiting

1. **Create a rate limit configuration:**
```bash
curl -X POST http://localhost:8080/api/rate-limits \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "client-1",
    "timeWindowRequests": 3,
    "timeWindowSeconds": 60,
    "monthlyRequests": 1000,
    "throttlingMode": "HARD"
  }'
```

2. **Send 3 requests (should succeed):**
```bash
for i in {1..3}; do
  curl -X POST http://localhost:8080/api/notifications/email \
    -H "X-Client-Id: client-1" \
    -H "Content-Type: application/json" \
    -d '{"recipient": "test@example.com", "message": "Test '$i'"}'
  echo ""
done
```

3. **Send 4th request (should fail with 429):**
```bash
curl -X POST http://localhost:8080/api/notifications/email \
  -H "X-Client-Id: client-1" \
  -H "Content-Type: application/json" \
  -d '{"recipient": "test@example.com", "message": "Test 4"}'
```

**Expected Result:** First 3 requests return 200, 4th request returns 429.

### Scenario 2: Soft Throttling

1. **Create configuration with soft throttling:**
```bash
curl -X POST http://localhost:8080/api/rate-limits \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "client-2",
    "timeWindowRequests": 2,
    "timeWindowSeconds": 60,
    "monthlyRequests": 1000,
    "throttlingMode": "SOFT"
  }'
```

2. **Send requests exceeding limit:**
```bash
for i in {1..5}; do
  curl -i -X POST http://localhost:8080/api/notifications/email \
    -H "X-Client-Id: client-2" \
    -H "Content-Type: application/json" \
    -d '{"recipient": "test@example.com", "message": "Test '$i'"}'
  echo ""
done
```

**Expected Result:** All requests return 200, but requests 3-5 include `X-RateLimit-Warning` header.

### Scenario 3: Monthly Rate Limiting

1. **Create configuration with low monthly limit:**
```bash
curl -X POST http://localhost:8080/api/rate-limits \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "client-3",
    "timeWindowRequests": 100,
    "timeWindowSeconds": 60,
    "monthlyRequests": 5,
    "throttlingMode": "HARD"
  }'
```

2. **Send 6 requests:**
```bash
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/notifications/email \
    -H "X-Client-Id: client-3" \
    -H "Content-Type: application/json" \
    -d '{"recipient": "test@example.com", "message": "Test '$i'"}'
  echo ""
done
```

**Expected Result:** First 5 requests succeed, 6th request fails with 429.

### Scenario 4: Global Rate Limiting

1. **Set low global limit:**
```bash
curl -X PUT http://localhost:8080/api/system/limits \
  -H "Content-Type: application/json" \
  -d '{"globalRequestsPerSecond": 3}'
```

2. **Send requests from multiple clients simultaneously:**
```bash
# Terminal 1
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/notifications/email \
    -H "X-Client-Id: client-a" \
    -H "Content-Type: application/json" \
    -d '{"recipient": "test@example.com", "message": "Test A'$i'"}'
  echo ""
done

# Terminal 2 (run simultaneously)
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/notifications/email \
    -H "X-Client-Id: client-b" \
    -H "Content-Type: application/json" \
    -d '{"recipient": "test@example.com", "message": "Test B'$i'"}'
  echo ""
done
```

**Expected Result:** Only 3 requests per second succeed across all clients.

### Scenario 5: Distributed System Testing

1. **Start multiple backend instances:**
```bash
# Terminal 1
cd backend
mvn spring-boot:run -Dserver.port=8080

# Terminal 2
cd backend
mvn spring-boot:run -Dserver.port=8081
```

2. **Send requests to different instances:**
```bash
# Request to instance 1
curl -X POST http://localhost:8080/api/notifications/email \
  -H "X-Client-Id: client-1" \
  -H "Content-Type: application/json" \
  -d '{"recipient": "test@example.com", "message": "Test"}'

# Request to instance 2
curl -X POST http://localhost:8081/api/notifications/email \
  -H "X-Client-Id: client-1" \
  -H "Content-Type: application/json" \
  -d '{"recipient": "test@example.com", "message": "Test"}'
```

**Expected Result:** Both instances share the same rate limit counters via Redis.

## Frontend Testing

1. **Start the frontend:**
```bash
cd frontend
npm install
ng serve
```

2. **Open browser:** `http://localhost:4200`

3. **Test scenarios:**
   - Create a new rate limit configuration
   - Update system global limits
   - Use the notification test interface to send multiple requests
   - Observe rate limit headers in browser dev tools

## Performance Testing

### Load Testing with Apache Bench

```bash
# Test with 1000 requests, 10 concurrent
ab -n 1000 -c 10 -H "X-Client-Id: test-client" \
   -p notification.json -T application/json \
   http://localhost:8080/api/notifications/email
```

### Load Testing with wrk

```bash
wrk -t12 -c400 -d30s -H "X-Client-Id: test-client" \
    --script=notification.lua \
    http://localhost:8080/api/notifications/email
```

## Monitoring

### Check Redis Keys
```bash
redis-cli
> KEYS rate_limit:*
> GET rate_limit:time_window:client-1
> TTL rate_limit:time_window:client-1
```

### Check Application Health
```bash
curl http://localhost:8080/actuator/health
```

### Check Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

## Troubleshooting

### Redis Connection Issues
- Ensure Redis is running: `redis-cli ping`
- Check Redis host/port in `application.yml`
- Check Redis logs

### Rate Limits Not Working
- Verify Redis is connected
- Check application logs for errors
- Verify rate limit configuration exists
- Check Redis keys: `redis-cli KEYS rate_limit:*`

### Frontend Not Connecting to Backend
- Check CORS configuration
- Verify backend is running on port 8080
- Check browser console for errors
- Verify API URL in frontend service

