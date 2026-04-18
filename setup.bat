@echo off
REM Setup script for Web Đọc Truyện

echo === Tao Web Doc Truyen ===

REM Backend setup
echo Install Backend...
cd backend
call npm install
copy .env.example .env
cd ..

REM Frontend setup
echo Install Frontend...
cd frontend
call npm install
copy .env.example .env
cd ..

REM Database setup
echo Setup Database...
echo Vui long chay cac lenh sau:
echo 1. createdb web_doc_truyen
echo 2. psql -U postgres -d web_doc_truyen -f database/schema.sql
echo 3. (Optional) psql -U postgres -d web_doc_truyen -f database/seed.sql

echo.
echo Setup thanh cong!
echo.
echo De khoi dong ung dung:
echo 1. Backend: cd backend ^&^& npm run dev
echo 2. Frontend: cd frontend ^&^& npm start

pause
