#Stage 1: build
FROM maven:3.9.9-amazoncorretto-21 AS build

# Copy src and pom.xml
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build scope
RUN mvn package -DskipTests


#Stage 2: create image
FROM amazoncorretto:21.0.4

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]