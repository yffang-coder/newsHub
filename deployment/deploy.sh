#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "🚀 Starting deployment..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found! Creating one with default values..."
    echo "MYSQL_PASSWORD=password" > .env
    echo "✅ Created .env file. Please update it with a secure password if needed."
fi

# Pull latest changes (optional, uncomment if using git)
# git pull origin main

# Build and start containers
echo "📦 Building and starting containers..."
docker-compose down
if [ "${DEPLOY_BUILD:-0}" = "1" ]; then
    docker-compose up -d --build
else
    docker-compose up -d
fi

echo "✨ Deployment completed successfully!"
echo "🌐 Frontend: http://localhost (or your server IP)"
echo "🔌 Backend API: Internal network only (access via Frontend)"
