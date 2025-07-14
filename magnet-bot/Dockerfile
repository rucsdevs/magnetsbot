FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app


COPY pom.xml .
COPY src ./src
COPY config.properties .


RUN mvn clean package -DskipTests


FROM openjdk:21

WORKDIR /app


COPY --from=build /app/target/magnet-bot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
COPY config.properties .


CMD ["java", "-jar", "app.jar"]

# How to run docker:
# Once your dockerfile has been constructred, use this set of commands to build, run, and check its status out. There exists many bash-based commands for running Docker, but these are just an example of some. Docker must be installed for this to work.
# sudo docker build -t magnet-bot .
# sudo docker run -d --name my-bot-container magnet-bot
# sudo docker logs -f my-bot-container