FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY target/nosql-auth.jar nosql-auth.jar

ENTRYPOINT ["java", "-jar", "/app/nosql-auth.jar"]

EXPOSE 8080
