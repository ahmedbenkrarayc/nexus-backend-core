# Build stage
FROM maven:3-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy all module poms
COPY nexus-boot/pom.xml nexus-boot/
COPY nexus-employee/pom.xml nexus-employee/
COPY nexus-notification/pom.xml nexus-notification/
COPY nexus-organization/pom.xml nexus-organization/
COPY nexus-project/pom.xml nexus-project/
COPY nexus-shared/pom.xml nexus-shared/
COPY nexus-staffing/pom.xml nexus-staffing/

RUN mvn -q dependency:go-offline

# Copy source code
COPY nexus-boot/src nexus-boot/src
COPY nexus-employee/src nexus-employee/src
COPY nexus-notification/src nexus-notification/src
COPY nexus-organization/src nexus-organization/src
COPY nexus-project/src nexus-project/src
COPY nexus-shared/src nexus-shared/src
COPY nexus-staffing/src nexus-staffing/src

# Build the project
RUN mvn -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/nexus-boot/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
