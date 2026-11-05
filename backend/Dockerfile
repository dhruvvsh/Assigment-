# ---- Build stage: compile and package the application ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only the build files first so dependency downloads are cached between builds.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q package -DskipTests

# ---- Run stage: a small JRE image with just the jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Do not run as root inside the container.
RUN useradd --system --no-create-home taskflow
USER taskflow

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
