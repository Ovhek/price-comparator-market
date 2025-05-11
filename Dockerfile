# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Download dependencies, this layer will be cached if pom.xml or .mvn doesn't change
RUN ./mvnw dependency:go-offline -B
COPY src ./src
# Build the application, run tests
RUN ./mvnw package

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Expose the debug port
EXPOSE 5005

# Define an argument for the JAR file name/path
ARG JAR_FILE=target/*.jar
# Copy the JAR from the builder stage
COPY --from=builder /app/${JAR_FILE} app.jar

# Expose the application port
EXPOSE 8080

# Set the entrypoint to run the application
# JAVA_TOOL_OPTIONS here to enable debugging.
# suspend=n: JVM starts immediately, debugger can attach anytime.
# suspend=y: JVM waits for debugger to attach before starting the app. 'n' is usually preferred.
# address=*:5005: Listen on all network interfaces within the container on port 5005.
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]