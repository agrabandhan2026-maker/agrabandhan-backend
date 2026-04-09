# AgraBandhan Backend

Matrimonial platform API for the Bisa Aggarwal Baniya community.

## Tech Stack

- **Java 21** + **Spring Boot 3.4**
- **PostgreSQL** (Neon free tier)
- **Redis** (Upstash free tier)
- **Cloudflare R2** (photo/media storage)
- **Firebase FCM** (push notifications)
- **JWT** (stateless authentication)

## Project Structure

```
com.agrabandhan
├── auth/           → OTP login, JWT, refresh tokens
├── profile/        → User profiles, family details, photos
├── matching/       → Weighted scoring engine, daily matches
├── search/         → Advanced filters, pagination
├── communication/  → Interests, chat, contact sharing
├── kundli/         → Horoscope, 36-Guna matching
├── notification/   → FCM push, email digests
├── admin/          → Moderation, user management, analytics
├── community/      → Samaj directory, endorsements, trust scores
└── common/         → Base entities, config, exceptions, DTOs
```

## Getting Started

### Prerequisites
- Java 21 (Temurin/Corretto)
- Maven 3.9+
- PostgreSQL 16+ (or Neon account)
- Redis (or Upstash account)

### Local Development

```bash
# Clone
git clone https://github.com/<your-org>/agrabandhan-backend.git
cd agrabandhan-backend

# Set environment variables (or create application-local.properties)
export DATABASE_URL=jdbc:postgresql://localhost:5432/agrabandhan_dev
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres

# Run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### API Documentation
Once running, access Swagger UI at:
```
http://localhost:8080/api/v1/swagger-ui.html
```

### Dev OTP
In dev mode, OTP for any phone number is: `123456`

## Deployment

The app deploys to **Render.com** (free tier) via Docker.

- Push to `main` → auto-deploys to production
- Push to `develop` → CI build only

### Environment Variables (Render Dashboard)

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | Neon PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | DB username |
| `DATABASE_PASSWORD` | DB password |
| `REDIS_URL` | Upstash Redis URL |
| `JWT_SECRET` | Auto-generated on first deploy |
| `R2_ENDPOINT` | Cloudflare R2 endpoint |
| `R2_ACCESS_KEY` | R2 access key |
| `R2_SECRET_KEY` | R2 secret key |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins |

## API Versioning

All endpoints are prefixed with `/api/v1/` via `server.servlet.context-path`.

## License

Proprietary. All rights reserved.
