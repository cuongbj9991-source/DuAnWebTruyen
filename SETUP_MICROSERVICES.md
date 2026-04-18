# Web Đọc Truyện - Microservices Setup Guide

## 🎯 Overview

Dự án **Web Đọc Truyện** được refactor thành architecture microservices:
- **Frontend**: React (Port 3000)
- **Story Service**: Java Spring Boot (Port 8080)
- **User Service**: .NET ASP.NET Core (Port 5001)
- **Database**: PostgreSQL (Port 5432)

## ✅ Prerequisites

Cài đặt các công cụ sau:

### 1. Node.js & npm
```bash
# Download from https://nodejs.org/
node --version  # v16 or higher
npm --version   # v7 or higher
```

### 2. Java Development Kit (JDK)
```bash
# Download from https://www.oracle.com/java/
java --version  # 17 or higher
mvn --version   # Maven 3.8+
```

### 3. .NET SDK
```bash
# Download from https://dot.microsoft.com/download
dotnet --version  # 8.0 or higher
```

### 4. PostgreSQL
```bash
# Download from https://www.postgresql.org/download/
psql --version  # 12 or higher
```

## 🗄️ Database Setup

### Step 1: Create Database
```bash
# Open PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE doctruyen;

# Exit
\q
```

### Step 2: Run Schema
```bash
# From project root
cd database

# Run schema
psql -U postgres -d doctruyen -f schema.sql

# Verify tables created
psql -U postgres -d doctruyen -c "\dt"
```

### Step 3: Seed Sample Data (Optional)
```bash
psql -U postgres -d doctruyen -f seed.sql
```

## 🚀 Backend Setup

### Java Spring Boot Backend

#### Step 1: Navigate to java-backend
```bash
cd java-backend
```

#### Step 2: Configure Database Connection
Create or update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/doctruyen
    username: postgres
    password: your-password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
  servlet:
    context-path: /api

jwt:
  secret: your-secret-key-for-jwt
  expiration: 86400000
```

#### Step 3: Build and Run
```bash
# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
mvn package
java -jar target/story-service-1.0.0.jar

# Server will start at http://localhost:8080/api
```

#### Troubleshooting
- If Maven not found: Add to PATH or use `./mvnw` (Maven Wrapper)
- If database connection fails: Check PostgreSQL is running and credentials are correct
- If port 8080 is busy: Change in `application.yml` -> `server.port`

### .NET ASP.NET Core Backend

#### Step 1: Navigate to dotnet-backend
```bash
cd dotnet-backend
```

#### Step 2: Configure Settings
Create or update `appsettings.json`:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Port=5432;Database=doctruyen;Username=postgres;Password=your-password;"
  },
  "JwtSettings": {
    "Secret": "your-very-long-secret-key-at-least-64-characters-for-production",
    "Issuer": "doctruyen-api",
    "Audience": "doctruyen-client",
    "DurationMinutes": 1440
  },
  "Cors": {
    "AllowedOrigins": ["http://localhost:3000"]
  }
}
```

#### Step 3: Run Migrations and Start
```bash
# Restore NuGet packages
dotnet restore

# Create database and run migrations
dotnet ef database update

# Run the application
dotnet run

# Server will start at http://localhost:5001/api
```

#### Troubleshooting
- If EF Core tools not installed: `dotnet tool install --global dotnet-ef`
- If migrations fail: Check database connection string
- If port 5001 is busy: Change in `appsettings.json`

## 💻 Frontend Setup

### Step 1: Navigate to frontend
```bash
cd frontend
```

### Step 2: Create Environment File
```bash
# Copy example
cp .env.example .env

# Edit .env with your backend URLs
cat > .env << EOF
REACT_APP_STORY_SERVICE=http://localhost:8080/api
REACT_APP_USER_SERVICE=http://localhost:5001/api
EOF
```

### Step 3: Install Dependencies
```bash
npm install
```

### Step 4: Start Development Server
```bash
npm start

# Frontend will open at http://localhost:3000
```

### Build for Production
```bash
npm run build
# Creates optimized build in 'build' folder
```

## 🧪 Testing the Application

### 1. Check All Services are Running
```bash
# Java backend health
curl http://localhost:8080/health 2>/dev/null || echo "Java backend not running"

# .NET backend health
curl http://localhost:5001/health 2>/dev/null || echo ".NET backend not running"

# Check frontend
open http://localhost:3000
```

### 2. Test Story Service
```bash
# Get all stories
curl http://localhost:8080/api/stories

# Get filter options
curl http://localhost:8080/api/stories/filter-options

# Search stories
curl "http://localhost:8080/api/stories/search?keyword=truyện"
```

### 3. Test User Service
```bash
# Register user
curl -X POST http://localhost:5001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# Login user
curl -X POST http://localhost:5001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 🔄 Development Workflow

### Terminal 1: Java Backend
```bash
cd java-backend
mvn spring-boot:run
```

### Terminal 2: .NET Backend
```bash
cd dotnet-backend
dotnet run
```

### Terminal 3: React Frontend
```bash
cd frontend
npm start
```

All three services should be running simultaneously.

## 📝 Environment Variables Summary

### Java Backend (application.yml)
- `spring.datasource.url` - PostgreSQL connection
- `spring.datasource.username` - DB username
- `spring.datasource.password` - DB password
- `server.port` - Server port (default: 8080)
- `jwt.secret` - JWT signing secret
- `jwt.expiration` - Token expiration in milliseconds

### .NET Backend (appsettings.json)
- `ConnectionStrings.DefaultConnection` - PostgreSQL connection
- `JwtSettings.Secret` - JWT signing secret (min 64 chars)
- `JwtSettings.DurationMinutes` - Token duration

### React Frontend (.env)
- `REACT_APP_STORY_SERVICE` - Java backend URL
- `REACT_APP_USER_SERVICE` - .NET backend URL

## 🐛 Common Issues & Solutions

### 1. PostgreSQL Connection Failed
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Start PostgreSQL
# Windows: Open PostgreSQL SQL Shell
# macOS: brew services start postgresql
# Linux: sudo systemctl start postgresql
```

### 2. Port Already in Use
```bash
# Find process using port (macOS/Linux)
lsof -i :8080
lsof -i :5001
lsof -i :3000

# Kill process
kill -9 <PID>

# Or change port in configuration
```

### 3. CORS Errors
- Check `.NET` backend has React frontend URL in `CORS.AllowedOrigins`
- Check `Java` backend has `@CrossOrigin` annotations on controllers
- Ensure cookies aren't being sent (if not needed)

### 4. JWT Token Issues
- Ensure JWT secret is at least 32 characters (preferably 64)
- Check token expiration time
- Verify token includes in Authorization header as "Bearer {token}"

### 5. Node Modules Issues
```bash
# Clean cache and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [ASP.NET Core Documentation](https://docs.microsoft.com/aspnet/core/)
- [React Documentation](https://react.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Introduction](https://jwt.io/introduction)

## 🎉 You're All Set!

Once all three services are running, you can:
1. Browse stories at http://localhost:3000
2. Search and filter stories
3. Register and login
4. Add stories to favorites
5. Write comments and ratings
6. Track reading progress

Happy reading! 📚✨
