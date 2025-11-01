FROM eclipse-temurin:17-jre

WORKDIR /app

# EC2에서 미리 빌드된 JAR 복사
COPY build/libs/*.jar /app/app.jar

ENV SERVER_PORT=8080
ENV SPRING_PROFILE="base,live,prod"
ENV JAVA_TOOL_OPTIONS=""

VOLUME ["/config-external"]

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -fsS "http://localhost:${SERVER_PORT}/health" || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_TOOL_OPTIONS} -jar /app/app.jar \
  --server.port=${SERVER_PORT} \
  --spring.profiles.active=${SPRING_PROFILE} \
  --spring.config.additional-location=file:/config-external/"]
