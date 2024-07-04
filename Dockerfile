# Use the official OpenJDK 17 image as a base
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled jar file into the container
COPY target/*.jar app.jar

# Command to run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
