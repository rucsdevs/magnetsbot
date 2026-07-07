# Build stage: compile and package the fat jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the pom first so dependency resolution is cached between builds
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

# Runtime stage: slim JRE image, no build tools, no secrets baked in
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/star.jar app.jar

# The token is provided at runtime (-e BOT_TOKEN=...), never copied into the image.
# The SQLite database lives in /data so it can be mounted as a volume.
ENV STAR_DB=/data/star.db
VOLUME /data

ENTRYPOINT ["java", "-jar", "app.jar"]
