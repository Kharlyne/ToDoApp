# Build-Phase: Maven verwendet JDK 17
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Laufzeit-Phase: nur fertige .jar ausführen
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/todo-backend-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 8888
CMD ["java", "-jar", "app.jar"]
