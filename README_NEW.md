# 📚 Web Đọc Truyện - Reading Application

> A modern web application for reading stories with advanced search, filtering, and user management.

## ✨ Features

### 📖 Story Management
- ✅ Browse thousands of stories
- ✅ Advanced search and filtering
  - Search by title, author, summary
  - Filter by genre, type, status, source
  - Sort by views, ratings, updates
- ✅ Story details and statistics
  - Views count
  - User ratings and reviews
  - Comments section

### 📖 Chapter Reading
- ✅ View all chapters of a story
- ✅ Chapter-by-chapter reading
- ✅ Track reading progress
- ✅ Save last read position

### 👤 User Features
- ✅ User registration and login
- ✅ User profiles
- ✅ Favorites system
- ✅ Reading history
- ✅ Comments and ratings
- ✅ JWT authentication

### 🎨 User Interface
- ✅ Responsive design
- ✅ Modern React components
- ✅ Intuitive navigation
- ✅ Real-time search
- ✅ Filter panel

## 🏗️ Architecture

### Microservices Architecture
```
Frontend (React)
    ├── Story Service (Java Spring Boot)
    │   ├── Stories
    │   ├── Chapters
    │   └── Comments
    └── User Service (.NET ASP.NET Core)
        ├── Authentication
        ├── Profiles
        ├── Favorites
        └── Reading Progress
```

### Technology Stack

#### Frontend
- **React 18** - UI framework
- **React Router v6** - Client-side routing
- **Axios** - HTTP client
- **CSS3** - Styling

#### Backend - Story Service (Java)
- **Spring Boot 3.2** - Framework
- **Spring Data JPA** - ORM
- **PostgreSQL** - Database
- **JWT** - Authentication

#### Backend - User Service (.NET)
- **ASP.NET Core 8** - Framework
- **Entity Framework Core** - ORM
- **PostgreSQL** - Database
- **JWT Bearer** - Authentication
- **AutoMapper** - Object mapping

#### Database
- **PostgreSQL 12+** - Relational database
- **6 tables** - Optimized schema

## 🚀 Quick Start

### Prerequisites
- Node.js v16+
- Java 17+
- .NET 8 SDK
- PostgreSQL 12+

### Installation

1. **Database Setup**
   ```bash
   createdb doctruyen
   psql -U postgres -d doctruyen -f database/schema.sql
   ```

2. **Java Backend**
   ```bash
   cd java-backend
   mvn spring-boot:run
   ```

3. **.NET Backend**
   ```bash
   cd dotnet-backend
   dotnet run
   ```

4. **React Frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```

See [SETUP_MICROSERVICES.md](./SETUP_MICROSERVICES.md) for detailed setup instructions.

## 📁 Project Structure

```
DuAnWebDocTruyen/
├── frontend/                    # React application
│   ├── src/
│   │   ├── components/         # Reusable components
│   │   ├── pages/             # Page components
│   │   ├── services/          # API services
│   │   ├── context/           # React Context
│   │   └── styles/            # CSS styles
│   └── package.json
│
├── java-backend/               # Spring Boot microservice
│   ├── src/main/java/
│   │   └── com/doctruyen/
│   │       ├── entity/        # JPA entities
│   │       ├── repository/    # Data access
│   │       ├── service/       # Business logic
│   │       ├── controller/    # REST endpoints
│   │       └── dto/           # Data objects
│   └── pom.xml
│
├── dotnet-backend/             # ASP.NET Core microservice
│   ├── Entities/              # Entity models
│   ├── DTOs/                  # Data transfer objects
│   ├── Services/              # Business logic
│   ├── Controllers/           # API endpoints
│   ├── Data/                  # DbContext
│   └── Program.cs
│
└── database/
    └── schema.sql             # Database schema
```

## 📊 Database Schema

### Tables
1. **users** - User accounts
2. **stories** - Story information
3. **chapters** - Story chapters
4. **comments** - User comments
5. **favorites** - Favorite stories
6. **reading_progress** - Reading progress tracking

## 🔌 API Documentation

### Story Service (Java - Port 8080)
```
GET    /api/stories                    # Get all stories
GET    /api/stories/search             # Search stories
GET    /api/stories/filter             # Filter stories
GET    /api/stories/filter-options     # Get filter options
GET    /api/stories/:id                # Get story details
GET    /api/chapters/story/:id         # Get chapters
GET    /api/comments/story/:id         # Get comments
POST   /api/comments                   # Create comment
```

### User Service (.NET - Port 5001)
```
POST   /api/auth/register              # Register user
POST   /api/auth/login                 # Login user
GET    /api/users/:id                  # Get profile
PUT    /api/users/:id                  # Update profile
GET    /api/favorites                  # Get favorites
POST   /api/favorites/:storyId         # Add favorite
DELETE /api/favorites/:storyId         # Remove favorite
GET    /api/reading-progress/:storyId  # Get progress
PUT    /api/reading-progress/:storyId  # Update progress
```

## 🔐 Authentication

- **JWT-based** authentication
- **Tokens** stored in localStorage
- **Protected routes** with authorization checks
- **Global auth state** with React Context

## 🎯 Use Cases

### For Readers
1. Browse and search for stories
2. Read chapters online
3. Rate and comment on stories
4. Keep track of reading progress
5. Save favorite stories
6. Manage reading history

### For Administrators
1. Manage story catalog
2. View user activity
3. Moderate comments
4. Monitor system performance

## 🚧 Removed Features

The application no longer includes:
- ❌ AI story generation
- ❌ OpenAI integration
- ❌ AI usage tracking
- ❌ Daily AI limits
- ❌ OTP email verification
- ❌ Cost management system

## 📈 Future Enhancements

- [ ] API Gateway for service routing
- [ ] Service discovery (Consul/Eureka)
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Search optimization (Elasticsearch)
- [ ] Caching layer (Redis)
- [ ] Message queue (RabbitMQ)
- [ ] Notifications service
- [ ] Analytics dashboard
- [ ] Advanced recommendations
- [ ] Mobile app

## 📚 Documentation

- [Refactored Architecture](./REFACTORED_ARCHITECTURE.md) - Detailed architecture overview
- [Microservices Setup Guide](./SETUP_MICROSERVICES.md) - Complete setup instructions
- [Original Setup Guide](./SETUP_GUIDE.md) - Legacy documentation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Project Team

Web Đọc Truyện Development Team

---

**Note**: This is a refactored version of Web Đọc Truyện with simplified microservices architecture. The original AI story generation features have been removed to focus on story reading and management.

For issues and feature requests, please create an issue in the repository.
