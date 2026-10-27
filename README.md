# IntelliTrust FinGuard 🛡️
### AI-Powered Financial Fraud Detection & Behavioral Security Platform

IntelliTrust FinGuard is an intelligent financial security platform designed to detect suspicious transactions, analyze spending patterns, assess transaction risks, and strengthen account security using machine learning and behavioral analytics.

The platform combines transaction monitoring, anomaly detection, geographical risk assessment, behavioral biometrics, and explainable risk scoring into a unified dashboard. It helps users understand potentially fraudulent activities through risk scores, alerts, and human-readable explanations.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [Fraud Detection Methodology](#-fraud-detection-methodology)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation and Setup](#-installation-and-setup)
- [Running the Application](#-running-the-application)
- [Using the Platform](#-using-the-platform)
- [API Documentation](#-api-documentation)
- [Security Considerations](#-security-considerations)
- [Limitations](#-limitations)
- [Future Enhancements](#-future-enhancements)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🚀 Overview

Traditional transaction monitoring systems often rely on fixed rules and predefined thresholds. Such approaches may struggle to adapt to changing spending habits, unusual transaction patterns, and suspicious behavioral activity.

IntelliTrust FinGuard addresses these challenges through a multi-factor risk assessment approach that evaluates financial transactions using several indicators.

The platform analyzes:

- Transaction amount and frequency.
- Unusual transaction patterns.
- Geographical risk indicators.
- Behavioral activity and authentication signals.
- Historical spending trends.

Based on these indicators, the system calculates a fraud risk score and provides interpretable results to help users investigate suspicious activity.

### Project Objectives

1. Detect potentially fraudulent financial transactions.
2. Identify anomalies in transaction behavior.
3. Provide interpretable fraud risk scores and alerts.
4. Analyze spending trends and forecast future expenditure.
5. Incorporate behavioral security signals into transaction risk assessment.
6. Provide a centralized dashboard for financial monitoring and security analysis.

---

## ✨ Key Features

### 1. Interactive Financial Dashboard

The dashboard provides a consolidated view of financial activity and security indicators.

Features include:
- Transaction summaries and statistics.
- Fraud risk indicators.
- Trust score visualization.
- Spending trends and forecasts.
- Suspicious transaction summaries.
- Security alerts and notifications.

### 2. Transaction Management

The platform supports transaction monitoring and analysis through its transaction management interface.

Capabilities include:
- Viewing transaction records.
- Importing transaction data from CSV files.
- Analyzing transaction amounts and frequencies.
- Identifying unusual transaction activity.
- Reviewing flagged transactions and their associated risk indicators.

### 3. AI-Based Fraud Detection

IntelliTrust FinGuard uses anomaly detection and multi-factor risk evaluation to identify potentially suspicious transactions.

The fraud detection process considers:
- Transaction amount.
- Transaction frequency.
- Statistical anomalies.
- Geographical risk.
- Behavioral risk indicators.

Each evaluated transaction receives a risk score that helps distinguish ordinary activity from potentially suspicious behavior.

### 4. Explainable Risk Assessment

Instead of presenting only a fraud probability or classification, the platform provides risk-related explanations.

These explanations help users understand which transaction characteristics or security indicators contributed to a suspicious classification.

Examples of risk indicators include:
- An unusually large transaction.
- An abnormal transaction frequency.
- An anomalous transaction pattern.
- An elevated geographical risk indicator.
- Unusual behavioral signals.

### 5. Dynamic Trust Scoring

The platform incorporates trust scoring to represent the assessed security risk associated with transaction activity.

Trust-related indicators can be evaluated alongside transaction risk to support security monitoring and investigation.

The goal is to make risk assessment more context-aware than a system based exclusively on fixed transaction limits.

### 6. Behavioral Biometrics

Behavioral security features incorporate interaction-related signals into risk assessment.

Potential indicators include:
- Typing behavior.
- Mouse interaction patterns.
- Deviations from expected behavioral activity.

These indicators can contribute to identifying activity that differs from an established behavioral pattern.

Behavioral signals should be interpreted as supporting risk indicators rather than definitive proof of unauthorized access.

### 7. Geographical Risk Analysis

The platform considers geographical risk indicators when evaluating transactions.

This functionality helps identify transactions that may warrant further investigation because of their location-related characteristics.

Geographical risk is one component of the overall risk assessment rather than an independent determination of fraud.

### 8. Spending Forecasts

The platform includes spending prediction functionality to help users understand their financial trends.

Forecasting capabilities support:
- Historical spending analysis.
- Identification of spending patterns.
- Estimation of future expenditure.
- Financial awareness and planning.

### 9. Fraud Simulation

The simulator supports testing fraud detection behavior under different transaction scenarios.

It can be used to explore how transaction characteristics affect risk scoring and classification.

Simulation results are intended for demonstration and evaluation rather than as evidence of production banking performance.

---

## 🛠️ Technology Stack

### Frontend
- React.js
- Vite
- JavaScript
- HTML5
- CSS3
- npm

### Backend
- Java 21
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- JSON Web Tokens (JWT)
- Maven

### Database
- PostgreSQL
- Hibernate / JPA

### Machine Learning and Analytics
- Isolation Forest-style anomaly detection
- Linear regression for spending prediction
- Multi-factor fraud risk scoring
- Behavioral and geographical risk indicators
- Explainable risk assessment

### Data Processing and API Tools
- Apache Commons CSV
- REST APIs
- Springdoc OpenAPI
- Swagger UI

---

## 🏗️ System Architecture

The application follows a frontend-backend architecture with a relational database.

```text
                 ┌──────────────────────────┐
                 │         User             │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │     React + Vite         │
                 │       Frontend           │
                 │                          │
                 │ Dashboard                │
                 │ Transactions             │
                 │ Risk Analysis            │
                 │ Spending Forecasts       │
                 └────────────┬─────────────┘
                              │
                         REST API
                              │
                              ▼
                 ┌──────────────────────────┐
                 │   Spring Boot Backend    │
                 │                          │
                 │ Authentication           │
                 │ Transaction Management   │
                 │ Fraud Risk Evaluation    │
                 │ Trust and Alert Logic    │
                 │ Spending Predictions     │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │   Risk Analysis Engine   │
                 │                          │
                 │ Anomaly Detection        │
                 │ Behavioral Indicators    │
                 │ Geographical Risk        │
                 │ Risk Score Calculation   │
                 │ Spending Forecasts       │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │       PostgreSQL         │
                 │                          │
                 │ Users and Transactions   │
                 │ Financial Records        │
                 │ Risk and Alert Data      │
                 └──────────────────────────┘
```

### Data Flow

1. The user interacts with the React frontend.
2. The frontend sends requests to the Spring Boot REST API.
3. The backend validates requests and applies authentication and authorization checks.
4. Transaction data is processed by the applicable risk assessment and analytics logic.
5. Risk indicators and transaction characteristics are used to calculate a fraud risk score.
6. Relevant records are stored or retrieved through PostgreSQL.
7. The backend returns results to the frontend.
8. The dashboard presents transaction information, risk indicators, and explanations.

---

## 🧠 Fraud Detection Methodology

IntelliTrust FinGuard uses a weighted risk scoring approach that combines multiple transaction and security indicators.

### Risk Score Calculation

The implemented scoring configuration assigns the following weights:

| Risk Indicator | Weight |
|---|---:|
| Transaction Amount | 30% |
| Transaction Frequency | 10% |
| Anomaly Detection | 25% |
| Geographical Risk | 25% |
| Behavioral Risk | 10% |
| **Total** | **100%** |

The overall score is calculated using the weighted contributions of the applicable risk indicators.

Conceptually:

```text
Fraud Risk Score =
    (Amount Risk × 0.30)
  + (Frequency Risk × 0.10)
  + (Anomaly Risk × 0.25)
  + (Geographical Risk × 0.25)
  + (Behavioral Risk × 0.10)
```

The component scores must be normalized to a consistent scale before being combined.

The resulting fraud risk score ranges from 0 to 100.

### Risk Classification

| Risk Score | Classification |
|---|---|
| 0–54 | Not flagged by the configured threshold |
| 55–74 | Medium Risk |
| 75–84 | High Risk |
| 85–100 | Critical Risk |

Transactions with scores of 55 or above are flagged for review according to the configured threshold.

**Note:** These thresholds are configurable application rules, not universally applicable fraud standards. A flagged transaction indicates potential risk and does not independently establish that fraud occurred.

### Anomaly Detection

The anomaly detection component identifies transaction patterns that differ from expected behavior.

An Isolation Forest-style approach can evaluate transaction characteristics and identify unusual observations.

### Spending Prediction

The spending prediction component uses linear regression to estimate future expenditure from available historical data.

Forecast accuracy depends on the quality, quantity, and representativeness of the input data.

---

## 📂 Project Structure

The repository is organized into separate frontend, backend, data, and tooling directories.

```text
IntelliTrust-FinGuard/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── index.html
│   └── ...
│
├── sample_data/
│   └── ...
│
├── tools/
│   └── ...
│
├── .gitignore
└── README.md
```

The exact contents of the `backend`, `frontend`, `sample_data`, and `tools` directories may vary with the current implementation.

---

## ⚙️ Prerequisites

Install the following software before running the application:

| Software | Purpose |
|---|---|
| Java JDK 21 | Runs the Spring Boot backend |
| Maven | Builds and runs the backend |
| Node.js | Runs the frontend tooling |
| npm | Installs frontend dependencies |
| PostgreSQL | Stores application data |
| Git | Clones and manages the repository |

Verify the installations:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
```

---

## 📥 Installation and Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/AKSHITAMODA/IntelliTrust-Finguard.git
```

Navigate into the project directory:

```bash
cd IntelliTrust-FinGuard
```

### Step 2: Configure PostgreSQL

Start PostgreSQL and create the application database.

Open the PostgreSQL command-line client:

```bash
psql -U postgres
```

Create the database:

```sql
CREATE DATABASE intellitrust_finguard;
```

Exit the PostgreSQL shell:

```sql
\q
```

Create a database user if you do not already have a suitable application user:

```sql
CREATE USER finguard_app WITH PASSWORD 'CHANGE_THIS_PASSWORD';
```

Grant database access:

```sql
GRANT ALL PRIVILEGES ON DATABASE intellitrust_finguard
TO finguard_app;
```

Depending on your PostgreSQL version and the application's schema configuration, additional schema privileges may be necessary.

### Step 3: Configure the Backend

Navigate to the backend directory:

```bash
cd backend
```

Configure your database connection and application secrets using your local application configuration.

The configuration should provide values equivalent to:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/intellitrust_finguard
spring.datasource.username=finguard_app
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
```

These are illustrative configuration entries. Preserve the project's existing required properties and adapt the entries to the application's actual configuration.

Set the corresponding environment variables before starting the backend.

**PowerShell:**

```powershell
$env:DB_PASSWORD = "YOUR_DATABASE_PASSWORD"
$env:JWT_SECRET = "YOUR_LONG_RANDOM_SECRET"
```

Use a sufficiently long, randomly generated JWT signing secret compatible with the algorithm configured by the application.

Do not commit real credentials to GitHub.

### Step 4: Build the Backend

From the `backend` directory, run:

```bash
mvn clean package
```

If the project includes tests, Maven will execute the configured test lifecycle.

Resolve any build or configuration errors before proceeding.

### Step 5: Install Frontend Dependencies

Open a new terminal and navigate to the frontend directory from the project root:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

### Step 6: Configure the Frontend

The frontend must use the correct backend API base URL.

For local development, the backend typically runs on:

```text
http://localhost:8080
```

The frontend development server typically runs on:

```text
http://localhost:5173
```

Configure the API URL according to the environment-variable names used by the existing frontend.

For Vite applications, frontend environment variables commonly use the `VITE_` prefix. Never place database passwords, JWT signing secrets, or other server-side secrets in frontend environment variables.

---

## ▶️ Running the Application

### Start the Backend

Open a terminal in the `backend` directory:

```bash
mvn spring-boot:run
```

Alternatively, if the project has been packaged successfully, run the generated JAR:

```bash
java -jar target/<generated-jar-name>.jar
```

Replace `<generated-jar-name>.jar` with the actual JAR filename in the `target` directory.

The backend should start on port `8080`, provided that the configured port is available.

### Start the Frontend

Open another terminal in the `frontend` directory:

```bash
npm run dev
```

Vite should display the local development URL in the terminal.

Open:

**http://localhost:5173**

The frontend should be able to communicate with the backend once the API URL, CORS configuration, database connection, and authentication settings are correct.

### Common Local URLs

| Component | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| Backend | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

The Swagger URL is available when the Springdoc OpenAPI integration is enabled and configured.

---

## 📊 Using the Platform

Once the application is running:

1. Open the frontend in your browser.
2. Register or log in using the application's authentication interface.
3. Navigate to the dashboard to view available financial and security indicators.
4. Explore transaction records and import supported CSV data.
5. Review risk scores and flagged transactions.
6. Inspect available explanations for transaction risk assessments.
7. Explore trust indicators, geographical risk, and behavioral security features.
8. Use the fraud simulator to test supported transaction scenarios.
9. Review spending trends and forecasts.

Available features depend on the current application configuration and the data provided.

---

## 🔌 API Documentation

The backend exposes REST endpoints for the application's supported operations.

The API layer is implemented using Spring Boot and Spring Web.

The application may provide endpoints for:

- User authentication.
- Transaction management.
- CSV transaction imports.
- Fraud risk analysis.
- Dashboard information.
- Alerts and risk explanations.
- Spending analytics and forecasts.

For the exact endpoints, request parameters, and response schemas, inspect the backend controllers or open Swagger UI when the backend is running:

```text
http://localhost:8080/swagger-ui/index.html
```

Authentication requirements and endpoint availability depend on the configured Spring Security rules.

---

## 🔐 Security Considerations

Security is an important part of IntelliTrust FinGuard.

The application incorporates security-related functionality such as:

- JWT-based authentication.
- Spring Security integration.
- Input validation.
- Transaction risk evaluation.
- Behavioral security indicators.
- Database-backed application data.
- Risk-related alerts and explanations.

### Security Best Practices

- Store credentials in environment variables or an appropriate secrets manager.
- Never commit `.env` files containing real secrets.
- Do not expose JWT signing keys in frontend code.
- Use HTTPS when deploying the application.
- Apply appropriate authorization checks to protected endpoints.
- Validate uploaded CSV files and their contents.
- Avoid logging passwords, access tokens, or sensitive financial information.
- Use synthetic or anonymized transaction data for demonstrations.
- Restrict access to sensitive records and administrative functionality.

The presence of security features does not by itself establish that an application is compliant with banking, financial-services, or data-protection regulations.

---

## ⚠️ Limitations

- Fraud detection results depend on the quality and completeness of the available transaction data.
- Risk thresholds may require calibration for different datasets and use cases.
- Behavioral indicators can be affected by legitimate changes in user behavior or device conditions.
- Spending forecasts may be inaccurate when historical data is limited or spending patterns change.
- Simulation results do not establish real-world fraud detection performance.
- The application is a financial security demonstration and is not a substitute for a production banking fraud detection system.
- The project does not establish a direct integration with real banking networks or live bank transaction processing.

---

## 🔮 Future Enhancements

Potential enhancements include:

- Advanced machine learning models for transaction classification.
- Improved behavioral anomaly detection.
- More sophisticated geographical risk analysis.
- Real-time transaction event processing.
- Email or mobile notifications for suspicious activity.
- Enhanced dashboard analytics.
- Model performance evaluation using precision, recall, F1-score, and ROC-AUC.
- False-positive analysis and risk threshold optimization.
- Improved audit logging and administrative monitoring.
- Integration with authorized financial data providers.
- Containerization and automated CI/CD deployment.
- Expanded automated testing and security testing.

These are potential future improvements and should not be interpreted as already implemented functionality.

---

## 🤝 Contributing

Contributions, bug reports, and suggestions are welcome.

To contribute:

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Commit your changes with a descriptive message.
5. Open a pull request describing the changes.

Example:

```bash
git checkout -b feature/your-feature
git add .
git commit -m "Add your feature"
git push origin feature/your-feature
```

---

## 📄 License

No license has been specified in this README.

Before distributing or reusing the project, add an appropriate license file if you intend to make the repository available under specific reuse terms.

---

## 👩‍💻 Author

**Akshita Moda**

GitHub: [@AKSHITAMODA](https://github.com/AKSHITAMODA)

**Project:** IntelliTrust FinGuard — AI-Powered Financial Fraud Detection & Behavioral Security Platform

---

*IntelliTrust FinGuard combines financial analytics, anomaly detection, and behavioral security to make transaction risk easier to identify, investigate, and understand.*
