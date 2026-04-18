#!/bin/bash

echo "=== Tạo Web Đọc Truyện ==="

# Backend setup
echo "📦 Cài đặt Backend..."
cd backend
npm install
cp .env.example .env
cd ..

# Frontend setup
echo "📦 Cài đặt Frontend..."
cd frontend
npm install
cp .env.example .env
cd ..

# Database setup
echo "🗄️ Cài đặt Database..."
echo "Vui lòng chạy các lệnh sau:"
echo "1. createdb web_doc_truyen"
echo "2. psql -U postgres -d web_doc_truyen -f database/schema.sql"
echo "3. (Optional) psql -U postgres -d web_doc_truyen -f database/seed.sql"

echo ""
echo "✅ Cài đặt hoàn tất!"
echo ""
echo "Để khởi động ứng dụng:"
echo "1. Backend: cd backend && npm run dev"
echo "2. Frontend: cd frontend && npm start"
