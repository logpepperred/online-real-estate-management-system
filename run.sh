#!/bin/bash
# ============================================================
# Online Real Estate Management System - Startup Script (macOS)
# ============================================================

# Ensure Homebrew path is in environment
export PATH="/opt/homebrew/bin:$PATH"

# Prefer IPv4 stack to prevent macOS network routing conflicts
export MAVEN_OPTS="-Djava.net.preferIPv4Stack=true"

echo "=================================================="
echo " Starting Online Real Estate Management System "
echo "=================================================="
echo "Using Maven: $(which mvn)"
echo "Java Version:"
java -version
echo "--------------------------------------------------"
echo "Launching Spring Boot application on port 8080..."
echo "Open your browser at: http://localhost:8080"
echo "--------------------------------------------------"

mvn spring-boot:run
