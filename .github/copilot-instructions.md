# Web Đọc Truyện - Development Instructions

## Project Overview
- **Name**: Web Đọc Truyện Chữ & Tranh
- **Tech Stack**: React (Frontend) + Node.js/Express (Backend) + PostgreSQL (Database)
- **Features**: Story listing, search/filter, reading, user accounts, favorites, reading progress, comments, external story scraping

## Setup Instructions

### Prerequisites
- Node.js v16+ 
- npm or yarn
- PostgreSQL 12+
- Git

### Getting Started

1. **Backend Setup**
   ```bash
   cd backend
   npm install
   ```
   - Create `.env` file with database credentials
   - Run migrations: `npm run migrate`
   - Start server: `npm run dev`

2. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```

3. **Database Setup**
   - Create PostgreSQL database
   - Run SQL scripts from `database/` folder
   - Update connection string in backend `.env`

### Project Structure
- `/backend` - Node.js/Express REST API
- `/frontend` - React application
- `/database` - PostgreSQL schemas and migrations
- `/docs` - Documentation files

### Development
- Backend runs on http://localhost:5000
- Frontend runs on http://localhost:3000
- Database: PostgreSQL (configure in `.env`)

### Key Features to Implement
- [ ] User authentication (JWT)
- [ ] Story CRUD operations
- [ ] Search and filtering
- [ ] Reading progress tracking
- [ ] Favorites system
- [ ] Comments and ratings
- [ ] Web scraping integration (Falool support)
