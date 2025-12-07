# API Rate Limiter System

A comprehensive rate limiting solution for Corporation X,Y,Z's notification service, supporting distributed deployments with Redis.

## Architecture Overview

The system implements a multi-layered rate limiting approach:
- **Time Window Rate Limiting**: Limits requests within a specific time window per client
- **Monthly Rate Limiting**: Tracks and limits requests per client on a monthly basis
- **Global Rate Limiting**: System-wide request limits across all clients
- **Distributed Support**: Uses Redis for shared state across multiple server instances
- **Throttling**: Implements both soft (warnings) and hard (rejection) throttling

## Prerequisites

- Java 17+
- Node.js 18+ and npm
- Redis 6.0+ 
- Maven 3.6+

##  Quick Start

### Backend Setup (Spring Boot)

1. Navigate to the backend directory:
```bash
cd backend
```

2. Ensure Redis is running:
```bash
# Using Docker
docker run -d -p 6379:6379 redis:latest

# Or install Redis locally and start it
```

3. Update Redis configuration in `src/main/resources/application.yml` if needed

4. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup (Angular)

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
ng serve
```

The frontend will be available at `http://localhost:4200`

##  Testing

### Backend Tests

Run all tests:
```bash
cd backend
mvn test
```

Run with coverage:
```bash
mvn test jacoco:report
```

View coverage report: `backend/target/site/jacoco/index.html`

### Frontend Tests
```bash
cd frontend
npm test or ng test
```

## API Endpoints

### Client Management
- `POST /api/clients` - Create a new client
- `GET /api/clients/{clientId}` - Get client by ID
- `GET /api/clients` - Get all clients
- `PUT /api/clients/{clientId}` - Update client information
- `DELETE /api/clients/{clientId}` - Delete a client

### Rate Limit Configuration
- `POST /api/rate-limits` - Create rate limit configuration
- `GET /api/rate-limits/{clientId}` - Get rate limit for a client
- `GET /api/rate-limits` - Get all rate limit configurations
- `PUT /api/rate-limits/{clientId}` - Update rate limit configuration
- `DELETE /api/rate-limits/{clientId}` - Delete rate limit configuration

### Notification Service (Protected)
- `POST /api/notifications/sms` - Send SMS notification (rate limited)
- `POST /api/notifications/email` - Send email notification (rate limited)

### System Configuration
- `GET /api/system/limits` - Get global system limits
- `PUT /api/system/limits` - Update global system limits

## Configuration

### Rate Limit Types

1. **Time Window Rate Limit**: Limits requests within a time window (e.g., 100 requests per minute)
2. **Monthly Rate Limit**: Limits total requests per month (e.g., 10,000 requests per month)
3. **Global Rate Limit**: System-wide limit across all clients

### Throttling Modes

- **Soft Throttling**: Returns warning headers but allows the request
- **Hard Throttling**: Rejects the request with 429 Too Many Requests

## Architecture Details

See `ARCHITECTURE.md` for detailed architecture documentation with diagrams.

## Environment Variables

### Backend
- `REDIS_HOST`: Redis host (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)
- `REDIS_PASSWORD`: Redis password (optional, required for Render/production)
- `REDIS_SSL`: Enable SSL for Redis (default: false, set to true for some Render Redis instances)
- `SPRING_PROFILES_ACTIVE`: Spring profile (use `prod` for production)
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL configuration (for production)

## Docker Support

### Run with Docker Compose
```bash
docker-compose up -d
```

This will start:
- Redis
- Spring Boot backend
- Angular frontend (production build)

## Monitoring

The system provides metrics endpoints:
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/health` - Health check

## Security

- API keys for client authentication
- CORS configuration for frontend
- Rate limit headers in responses

## Additional Documentation

- [Render Deployment Guide](./RENDER_DEPLOYMENT.md) - Step-by-step guide for deploying to Render
- [API Documentation](./API_DOCUMENTATION.md) - Complete API reference with Swagger UI
- [Testing Guide](./TESTING_GUIDE.md) - Comprehensive testing scenarios

