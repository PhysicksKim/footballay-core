# ========================================
# Multi-stage Dockerfile for Footballay Core
# ========================================
# Stage 1 (Builder): Compile and test the application
# Stage 2 (Runner): Run the application with minimal footprint
#
# Design principles:
# - Secrets are NOT included in the image
# - Configuration is mounted at runtime via /config-external volume
# - Image is tagged with timestamp + git SHA for versioning
# - Memory-constrained for t3.small EC2 (use JAVA_TOOL_OPTIONS for heap limit)
# ========================================

# ========================================
# Stage 1: Builder
# ========================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradlew .
COPY gradle/ gradle/
# Support both Groovy (build.gradle) and Kotlin DSL (build.gradle.kts)
COPY build.gradle* .
COPY settings.gradle* .
COPY gradle.properties* .

# Ensure gradlew is executable (Git may not preserve execute permission in Docker context)
RUN chmod +x gradlew

# Pre-cache dependencies (this layer will be cached if build files don't change)
# The '|| true' prevents build failure if dependencies task doesn't exist
RUN ./gradlew --no-daemon dependencies || true

# Copy source code
COPY src/ src/

# Build JAR (skip tests to avoid Testcontainers issues in Docker build)
# Note: Tests should be run separately in CI/CD or locally before building the image
# - compileTestKotlin/compileTestJava: Ensures test code compiles (catches compilation errors)
# - bootJar: Creates the executable JAR
# - -x test: Skips test execution (Testcontainers won't work in Docker build environment)
#
# Why skip tests here?
# 1. Integration tests use Testcontainers which requires Docker access
# 2. Docker-in-Docker is complex and not recommended for image builds
# 3. Tests should run in CI/CD pipeline before pushing code
# 4. This keeps build fast and avoids Docker socket mounting issues
RUN ./gradlew --no-daemon clean compileKotlin compileTestKotlin bootJar -x test

# ========================================
# Stage 2: Runner
# ========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/build/libs/*.jar /app/app.jar

# Environment variables with defaults
# These can be overridden at container runtime:
# - SERVER_PORT: The port Spring Boot will listen on (8081 for blue, 8082 for green)
# - SPRING_PROFILE: The Spring profile to activate (dev for local, live for production)
# - JAVA_TOOL_OPTIONS: JVM options (e.g., "-Xmx512m" for memory-constrained environments)
ENV SERVER_PORT=8080
ENV SPRING_PROFILE=live
ENV JAVA_TOOL_OPTIONS=""

# Volume for external configuration
# Mount /srv/footballay/config-live (on EC2) to /config-external (in container)
# This directory should contain:
# - application-secret.yml (DB credentials, JWT keys, Loki credentials)
# - application-path.yml (file paths)
# - application-aws.yml (AWS S3/CloudFront settings)
# - application-api.yml (ApiSports API keys)
VOLUME ["/config-external"]

# Expose the application port
# NOTE: This is documentation only. Actual port mapping is done via docker run -p
EXPOSE 8080

# Health check is performed by deploy.sh script, not by Docker
# Reason: eclipse-temurin:21-jre doesn't include curl by default
# and installing it would increase image size unnecessarily.
# The deployment script performs comprehensive health checks after container start.

# Entry point
# Key flags:
# - --server.port=${SERVER_PORT}: Use the port from environment variable
# - --spring.profiles.active=${SPRING_PROFILE}: Activate the specified profile group
# - --spring.config.additional-location=/config-external/: Load external config files
#   This merges/overrides settings from classpath configs with externally mounted secrets
ENTRYPOINT ["sh", "-c", "java ${JAVA_TOOL_OPTIONS} -jar /app/app.jar --server.port=${SERVER_PORT} --spring.profiles.active=${SPRING_PROFILE} --spring.config.additional-location=file:/config-external/"]
