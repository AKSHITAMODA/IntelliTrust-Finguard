# IntelliTrust FinGuard

AI-driven fraud detection and financial behaviour security platform. Java 21 + Spring Boot 3
backend, React (Vite) frontend, PostgreSQL, JWT auth, and a self-contained hybrid fraud/ML
engine (Isolation Forest + linear regression, both implemented in plain Java for full
explainability and zero external ML-library risk).

## Architecture

```
controller -> service -> repository -> entity
                 |
                 +-- ml (SimpleLinearRegression, IsolationForestLite)
                 +-- security (JWT filter, Spring Security)
                 +-- config (CORS, OpenAPI, demo data seeding)
                 +-- exception (global handler)
```

## Prerequisites

- Java 21 (`java -version`)
- PostgreSQL running locally (a `postgresql-x64-17` Windows service is assumed; adjust connection
  details in `backend/.env.example` / `application.yml` if yours differs)
- Node.js 18+ and npm
- Maven — a local copy is already vendored at `tools/apache-maven-3.9.9` in this repo if `mvn`
  isn't on your PATH; use `tools/apache-maven-3.9.9/bin/mvn` in place of `mvn` below.

## Database setup

```sql
CREATE ROLE finguard_app WITH LOGIN PASSWORD 'finguard_app_pw';
CREATE DATABASE intellitrust_finguard OWNER finguard_app;
```

(Already created for this workstation via `psql -U postgres`.)

## Backend

```bash
cd backend
mvn spring-boot:run
```

Runs on **http://localhost:8080**. On first boot it seeds a demo account and six months
(Mar-Aug, relative to today) of realistic transaction history automatically
(`DEMO_SEED_ON_STARTUP=true` by default).

Config is environment-driven (see `backend/.env.example`): `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`JWT_SECRET`, `JWT_EXPIRATION_MS`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`.

Run tests:

```bash
mvn test
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on **http://localhost:5173**. Set `VITE_API_URL` (see `frontend/.env.example`) if the
backend isn't on `localhost:8080`.

## Demo credentials

```
Email:    demo@finguard.com
Password: Demo@1234
```

The demo account starts with ~₹84,500 net balance, ~₹18,500 last-month spending, trust score
~100, and zero alerts. Use **Profile → Reset Demo Data** to restore this baseline at any time
(also available as `POST /api/demo/reset` while authenticated).

## Swagger / OpenAPI

**http://localhost:8080/swagger-ui.html**

Click "Authorize" and paste `Bearer <token>` (obtained from `/api/auth/login`) to call protected
endpoints directly from the Swagger UI.

## API groups

| Group | Base path |
|---|---|
| Auth | `/api/auth` (register, login, me) |
| Dashboard | `/api/dashboard` |
| Transactions | `/api/transactions` (CRUD, filtering) |
| CSV Import | `/api/transactions/import` |
| Simulator | `/api/transactions/simulate` |
| Fraud | `/api/fraud` (explain/{id}, summary) |
| Forecast | `/api/forecast` |
| Behaviour | `/api/behaviour` (event, status) |
| Location | `/api/location` |
| Trust Score | `/api/trust-score` |
| Alerts | `/api/alerts` |
| Demo | `/api/demo/reset` |

## Fraud engine

Every DEBIT transaction is scored 0-100 as a weighted blend of five explainable signals:

- **Amount risk** (30%) — deviation from the median of your last 120 debit transactions, plus
  flat thresholds above ₹50,000 / ₹100,000.
- **Frequency risk** (10%) — number of transactions in the trailing 1-hour window.
- **Anomaly score** (25%) — a from-scratch Isolation Forest (Liu, Ting & Zhou, 2008) trained
  per-user on amount + cyclical time-of-day features.
- **Geo risk** (25%) — Haversine great-circle distance between the transaction and your most
  recent live-location reading.
- **Behavioural risk** (10%) — your latest typing/mouse/click deviation score.

Transactions scoring ≥ 55 are flagged fraud, generate an `Alert`, and reduce the account's trust
score. Every flagged transaction stores a human-readable reason string, and `/api/fraud/explain/{id}`
recomputes the full breakdown on demand for the UI's explainability panel.

## Behavioural biometrics privacy note

The frontend's `BehaviourTracker` only ever reads **timestamps** of key-down events (to derive
typing speed and key interval), mouse-movement deltas (for speed), and click counts. It never
reads, transmits, or stores which keys were pressed.

## Remaining / known limitations

- The Isolation Forest and linear regression are re-fit on each request (bounded to ≤120/12
  history rows) rather than persisted/versioned models — adequate for this prototype's scale.
- No email verification or password-reset flow (out of scope for this milestone).
- Frontend build (`npm run build`) and backend `mvn spring-boot:run` were both smoke-tested;
  see the assistant's final summary for anything flagged as unverified in your environment.
