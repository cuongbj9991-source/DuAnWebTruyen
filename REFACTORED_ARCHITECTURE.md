# Web Đọc Truyện - Refactored Architecture

## 🏗️ Architecture Overview

Dự án đã được refactor thành kiến trúc microservices với các thành phần sau:

```
┌─────────────────────────────────┐
│    React Frontend (Port 3000)   │
└────────────┬────────────────────┘
             │
      ┌──────┴──────┐
      │             │
      ▼             ▼
┌─────────────┐  ┌──────────────┐
│ Java        │  │ .NET         │
│ Spring Boot │  │ ASP.NET Core │
│ (Port 8080) │  │ (Port 5001)  │
│             │  │              │
│ Story       │  │ User & Auth  │
│ Chapter     │  │ Favorites    │
│ Comments    │  │ Preferences  │
└─────────────┘  └──────────────┘
      │             │
      └──────┬──────┘
             │
      ┌──────▼──────┐
      │ PostgreSQL  │
      │ (Port 5432) │
      └─────────────┘
```

## 📋 Project Changes

### ✅ Removed Features
- AI Story Generation (OpenAI integration)
- OTP-based registration
- AI usage tracking and logging
- Daily AI limits
- Cost management system

### 📚 What's Left
- Story browsing and searching
- Advanced filtering by genre, type, status
- Chapter reading
- User comments and ratings
- Favorites system
- Reading progress tracking
- User authentication

## 🚀 Getting Started

### Prerequisites
- Node.js v16+ (for React)
- Java 17+ (for Spring Boot)
- .NET 8 SDK (for ASP.NET Core)
- PostgreSQL 12+

### 1. Database Setup

```bash
# Create database
createdb doctruyen

# Run schema
psql -U postgres -d doctruyen -f database/schema.sql
```

### 2. Java Spring Boot Backend (Story Service)

```bash
cd java-backend

# Install dependencies & build
mvn clean install

# Run the application
mvn spring-boot:run
# Server runs on http://localhost:8080/api
```

### 3. .NET ASP.NET Core Backend (User Service)

```bash
cd dotnet-backend

# Restore dependencies
dotnet restore

# Create migrations & database
dotnet ef database update

# Run the application
dotnet run
# Server runs on http://localhost:5001/api
```

### 4. React Frontend

```bash
cd frontend

# Install dependencies
npm install

# Create .env file
cat > .env << EOF
REACT_APP_STORY_SERVICE=http://localhost:8080/api
REACT_APP_USER_SERVICE=http://localhost:5001/api
REACT_APP_API_GATEWAY=http://localhost:8000/api
EOF

# Start development server
npm start
# Open http://localhost:3000
```

## 📁 Project Structure

```
DuAnWebDocTruyen/
├── frontend/                 # React application
│   ├── src/
│   │   ├── components/       # Reusable components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   ├── context/         # React Context (Auth)
│   │   └── styles/          # CSS files
│   └── package.json
│
├── java-backend/            # Spring Boot microservice
│   ├── src/main/java/com/doctruyen/
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST endpoints
│   │   └── StoryServiceApplication.java
│   ├── pom.xml
│   └── application.yml
│
├── dotnet-backend/          # ASP.NET Core microservice
│   ├── Entities/            # Entity models
│   ├── DTOs/                # Data transfer objects
│   ├── Services/            # Business logic
│   ├── Controllers/         # API endpoints
│   ├── Data/                # Database context
│   ├── Mappings/            # AutoMapper profiles
│   ├── Program.cs           # Entry point
│   ├── appsettings.json
│   └── DocTruyen.UserService.csproj
│
├── database/
│   ├── schema.sql          # Database schema
│   └── seed.sql            # Sample data (optional)
│
└── docs/                    # Documentation
```

## 🔌 API Endpoints

### Story Service (Java - Port 8080)
- `GET /api/stories` - Get all stories with pagination
- `GET /api/stories/search?keyword=...` - Search stories
- `GET /api/stories/filter?genre=...&status=...` - Filter stories
- `GET /api/stories/filter-options` - Get filter options
- `GET /api/stories/:id` - Get story details
- `GET /api/chapters/story/:storyId` - Get all chapters
- `GET /api/chapters/story/:storyId/chapter/:number` - Get specific chapter
- `GET /api/comments/story/:storyId` - Get story comments
- `POST /api/comments` - Create comment
- `PUT /api/comments/:id` - Update comment
- `DELETE /api/comments/:id` - Delete comment

### User Service (.NET - Port 5001)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/users/:id` - Get user profile
- `PUT /api/users/:id` - Update user profile
- `GET /api/users/:id/reading-history` - Get reading history
- `GET /api/favorites` - Get user's favorites
- `POST /api/favorites/:storyId` - Add to favorites
- `DELETE /api/favorites/:storyId` - Remove from favorites
- `GET /api/reading-progress/story/:storyId` - Get reading progress
- `PUT /api/reading-progress/story/:storyId` - Update reading progress

## 📝 Configuration

### Java Backend (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/doctruyen
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
```

### .NET Backend (appsettings.json)
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Port=5432;Database=doctruyen;Username=postgres;Password=postgres;"
  },
  "JwtSettings": {
    "Secret": "your-secret-key-change-in-production-very-long-string",
    "Issuer": "doctruyen-api",
    "Audience": "doctruyen-client",
    "DurationMinutes": 1440
  }
}
```

### React Frontend (.env)
```
REACT_APP_STORY_SERVICE=http://localhost:8080/api
REACT_APP_USER_SERVICE=http://localhost:5001/api
```

## 🔐 Authentication

- JWT-based authentication
- Tokens stored in localStorage
- Tokens included in Authorization headers for protected endpoints
- User context managed globally with React Context

## 📦 Technologies Used

### Frontend
- React 18
- React Router v6
- Axios (HTTP client)
- CSS3

### Java Backend
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- JWT (jjwt)
- Lombok
- jsoup (web scraping)

### .NET Backend
- ASP.NET Core 8
- Entity Framework Core
- PostgreSQL (Npgsql)
- JWT Bearer authentication
- AutoMapper
- FluentValidation

## 📊 Database Schema

### Tables
- **users** - User accounts
- **stories** - Story information
- **chapters** - Story chapters
- **comments** - User comments on stories
- **favorites** - User's favorite stories
- **reading_progress** - User's reading progress

## 🚧 Future Enhancements

- API Gateway for routing between services
- Service discovery (Consul, Eureka)
- Docker containerization
- Kubernetes orchestration
- Search optimization (Elasticsearch)
- Caching layer (Redis)
- Message queue (RabbitMQ, Kafka)
- Notifications service

## 📄 License

This project is licensed under the MIT License.

## 👥 Contributors

Web Đọc Truyện Development Team
