@echo off
echo Starting NewsHub Backend in LOCAL mode...
echo (Using H2 Database, Mock Redis, Mock Kafka)
echo.

cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.profiles=local

pause
