# Stage 1: Initial setup
FROM gradle:7.2.0-jdk11 AS builder
WORKDIR /app
COPY . .
ENV DOCKER_HOST=tcp://host.docker.internal:2375
ENV TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal

# Stage 2: Gradle Build
RUN gradle build --no-daemon
RUN gradle shadowJar

# Stage 3: Final image
FROM azul/zulu-openjdk:11.0.15
WORKDIR /app
RUN ls -la
COPY --from=builder /app/paygrind-app/build/libs/paygrind-app-1.0-all.jar .
ENTRYPOINT ["java","-jar","paygrind-app-1.0-all.jar"]