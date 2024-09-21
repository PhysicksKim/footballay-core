FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/score-board-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev,api,cookies,devmariadb

ENTRYPOINT ["java", "-jar", "app.jar"]
