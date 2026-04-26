#!/bin/bash

# Default ports
export DUDU_MONGO_PORT=27017
export DUDU_MYSQL_PORT=3306
START_MONGO=true
START_MYSQL=true

# Function to check if a port is in use
check_port() {
    # Try ss, then netstat, then lsof
    if command -v ss >/dev/null 2>&1; then
        ss -tuln | grep -q ":$1 "
    elif command -v netstat >/dev/null 2>&1; then
        netstat -tuln | grep -q ":$1 "
    else
        lsof -i :$1 >/dev/null 2>&1
    fi
}

# Function to check if a port is managed by Docker
check_is_docker() {
    docker ps --format '{{.Ports}}' | grep -q ":$1->"
}

# Function to identify MongoDB
check_is_mongo() {
    ps aux | grep -E "mongod|mongodb" | grep -v grep > /dev/null
}

# Function to identify MySQL
check_is_mysql() {
    ps aux | grep -E "mysqld|mysql" | grep -v grep > /dev/null
}

echo "=== ∂u∂u Environment Pre-flight Check ==="

# 1. MongoDB Logic (27017)
if check_port 27017; then
    if check_is_docker 27017; then
        echo "[DOCKER] Existing Docker MongoDB detected on 27017."
        START_MONGO=false
    elif check_is_mongo; then
        echo "[MATCH] Host MongoDB found on 27017. Will use host instance."
        START_MONGO=false
        export DUDU_MONGO_PORT=27017
    else
        echo "[CONFLICT] Port 27017 is occupied by another app. Shifting Docker MongoDB to 27018."
        export DUDU_MONGO_PORT=27018
    fi
else
    echo "[FREE] Port 27017 is available. Using default for Docker MongoDB."
fi

# 2. MySQL Logic (3306)
if check_port 3306; then
    if check_is_docker 3306; then
        echo "[DOCKER] Existing Docker MySQL detected on 3306."
        START_MYSQL=false
    elif check_is_mysql; then
        echo "[MATCH] Host MySQL found on 3306. Will use host instance."
        START_MYSQL=false
        export DUDU_MYSQL_PORT=3306
    else
        echo "[CONFLICT] Port 3306 is occupied by another app. Shifting Docker MySQL to 3307."
        export DUDU_MYSQL_PORT=3307
    fi
else
    echo "[FREE] Port 3306 is available. Using default for Docker MySQL."
fi

# 3. Docker Orchestration
SERVICES=""
[ "$START_MONGO" = true ] && SERVICES="$SERVICES mongodb"
[ "$START_MYSQL" = true ] && SERVICES="$SERVICES mysql"

if [ -n "$SERVICES" ]; then
    echo "[DOCKER] Starting required services:$SERVICES"
    docker-compose up -d $SERVICES
else
    echo "[DOCKER] No services need to be started (using host databases)."
fi

echo "[BUILD] Building ∂u∂u Framework..."
mvn clean install -DskipTests

echo "=== Setup Complete ==="
echo "DUDU_MONGO_PORT=$DUDU_MONGO_PORT"
echo "DUDU_MYSQL_PORT=$DUDU_MYSQL_PORT"
echo "=========================="
echo "You can now run the framework using:"
echo "mvn exec:java -Dexec.mainClass=\"org.dudu.Main\""
