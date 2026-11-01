# TinyURL Generator

A URL shortener with click analytics: Spring Boot 4 (Java 21), PostgreSQL and Redis on the backend, React 19 + Vite + Tailwind on the frontend.

- Shorten any URL, with an optional custom alias and expiry in days
- `302` redirect that records every click (time, IP, user agent)
- Analytics per link: total clicks, last 24 hours, last 7 days, last access
- Nightly cleanup job that deletes expired links and stale ones

## Architecture

```mermaid
graph LR
    UI[React + Vite] -->|POST /api/v1/shorten<br/>GET /api/v1/analytics/:code| API
    Browser -->|GET /:code| API
    subgraph Spring Boot
        API[UrlController] --> S[UrlService]
        S --> B[Base62Service]
        S --> L[UrlLookUpService<br/>@Cacheable]
        S -.->|@Async| K[ClickRecorder]
        L --> R[JPA repositories]
        K --> R
        C[UrlCleanupService<br/>@Scheduled 02:00] --> R
    end
    L --> RD[(Redis<br/>urls cache)]
    R --> DB[(PostgreSQL<br/>urls, url_click)]
```

## How it works

**Short codes.** A generated code is 7 random Base62 characters (`0-9a-zA-Z`) from `SecureRandom`, about 3.5 trillion possibilities, so codes cannot be guessed by counting. If a code is already taken it retries, up to 5 times; the `unique` constraint on `short_code` catches the rare race between two requests. A custom alias is 3-20 characters of letters, digits, `-` and `_`, and a taken alias returns `409`.

**Redirects.** The lookup `shortCode → longUrl` is cached in Redis for 10 minutes, so most redirects never touch Postgres. Expiry is checked after the lookup, so a cached link still returns `410` once it expires. The click is then recorded on a background thread (`@Async`): `click_count` is incremented in SQL (`click_count + 1`), so concurrent clicks are never lost.

**Client IP.** `server.forward-headers-strategy=native` lets Tomcat read `X-Forwarded-For` only when the request comes from a trusted proxy (private and loopback addresses by default; set `server.tomcat.remoteip.internal-proxies` to your load balancer in production). Anyone else's header is ignored.

**Why 302 and not 301.** Browsers cache a `301` and stop asking the server, so later clicks would go uncounted. A `302` sends every click through the API.

**Cleanup in two tiers** (runs at 02:00 every day):
1. Links whose `expiresAt` has passed.
2. Links not opened for 3 months that have fewer than 10 clicks.

Clicks are deleted before their links, so the foreign key from `url_click` to `urls` is never broken. The job also clears the Redis cache, so deleted links stop redirecting at once.

**Schema.** Flyway owns the schema (`src/main/resources/db/migration`); Hibernate only validates it (`ddl-auto=validate`). An existing database created before Flyway is baselined at V1, so only newer migrations run on it.

## API

| Method | Path | Body / result |
|---|---|---|
| `POST` | `/api/v1/shorten` | `{"longUrl": "https://...", "customAlias": "opt", "expiryDays": 7}` → `201` with `shortUrl`, `shortCode`, `expiresAt` |
| `GET` | `/{shortCode}` | `302` to the long URL; `404` if unknown; `410 Gone` if expired |
| `GET` | `/api/v1/analytics/{shortCode}` | `totalClicks`, `clicksLast24Hours`, `clicksLast7Days`, `lastAccessedAt` |

Invalid input is rejected with `400` and field messages, e.g. `{"longUrl": "Must be a valid URL"}`.

## Run it locally

You need Java 21, Docker and Node 20+.

```bash
# 1. Postgres and Redis (set PG_PORT if 5432 is taken locally)
docker compose up -d
#    e.g. a Postgres already installed on the machine:
#    PG_PORT=5433 docker compose up -d

# 2. Backend on http://localhost:8080
./mvnw spring-boot:run
#    If you changed PG_PORT, also pass the matching URL:
#    DB_URL=jdbc:postgresql://localhost:5433/tinyurl ./mvnw spring-boot:run

# 3. Frontend on http://localhost:5173
cd frontend && npm install && npm run dev
```

Configuration comes from environment variables: `DB_URL`, `DB_USER`, `DB_PASS`, `REDIS_HOST`, `REDIS_PORT`, and `APP_BASE_URL` (the host used to build `shortUrl`). The defaults match `docker-compose.yml`.

## Trade-offs

- Clicks waiting on the background thread are lost if the app crashes. At higher traffic, send click events to a durable queue (Kafka, Redis Streams) and batch-insert them.
- `127.0.0.1` counts as a trusted proxy, so locally a request can still set its own `X-Forwarded-For`. Narrow `internal-proxies` in production.
