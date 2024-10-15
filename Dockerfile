# OpenJDK 17 imajını kullan
FROM openjdk:17-jdk-slim

# Çalışma dizinini ayarla (burada /app yerine başka bir dizin ismi kullanabilirsiniz)
WORKDIR /usr/src/app

# JAR dosyasını kopyala
COPY target/nosql-auth-0.0.1-SNAPSHOT.jar ./nosql-auth.jar

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "nosql-auth.jar"]

# Portu aç
EXPOSE 8080
