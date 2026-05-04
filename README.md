# Job Tracker Backend

Spring Boot REST API for the Smart Job Application Tracker. Uses Claude AI (Anthropic) to analyze resume-to-job-description fit and provide actionable suggestions.

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- WebFlux (for Anthropic API calls)
- Lombok
- Maven
- Docker

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/applications` | Get all applications (filter by `?status=APPLIED`) |
| GET | `/api/applications/{id}` | Get application by ID |
| POST | `/api/applications` | Create new application (auto-analyzes if resume + JD provided) |
| PUT | `/api/applications/{id}` | Update application |
| DELETE | `/api/applications/{id}` | Delete application |
| POST | `/api/applications/{id}/analyze` | Re-run AI analysis on saved application |
| POST | `/api/applications/analyze` | Quick analyze without saving |
| GET | `/api/applications/stats` | Get dashboard stats |

## Application Status Flow

`SAVED` → `APPLIED` → `PHONE_SCREEN` → `INTERVIEW` → `OFFER` / `REJECTED` / `WITHDRAWN`

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+ (or Docker)
- Anthropic API key ([get one here](https://console.anthropic.com))

### Option 1: Run with Docker Compose (Recommended)

```bash
# Clone the repo
git clone https://github.com/yourusername/job-tracker-backend.git
cd job-tracker-backend

# Set your Anthropic API key
cp .env.example .env
# Edit .env and add your ANTHROPIC_API_KEY

# Start everything
docker-compose up --build
```

API will be running at `http://localhost:8080`

### Option 2: Run Locally

```bash
# Start PostgreSQL (or use Docker just for the DB)
docker run -d -e POSTGRES_DB=jobtracker -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -p 5432:5432 postgres:15-alpine

# Set environment variables
export ANTHROPIC_API_KEY=your_key_here

# Run the app
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

## Example Request

### Create Application with AI Analysis

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Google",
    "jobTitle": "Senior Software Engineer",
    "jobUrl": "https://careers.google.com/...",
    "location": "Mountain View, CA",
    "jobDescription": "We are looking for a Senior Java Engineer with experience in distributed systems...",
    "resumeText": "Manasa Thipparthi - Senior Software Engineer with 10+ years...",
    "notes": "Referral from John"
  }'
```

### Quick Analyze (without saving)

```bash
curl -X POST http://localhost:8080/api/applications/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "jobDescription": "Looking for a Java engineer with Spring Boot...",
    "resumeText": "Your resume text here..."
  }'
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/jobtracker` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `password` |
| `ANTHROPIC_API_KEY` | Your Anthropic API key | **Required** |
| `CORS_ALLOWED_ORIGINS` | Frontend URL for CORS | `http://localhost:3000` |
