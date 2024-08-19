FROM openjdk:17-jdk-alpine

ARG JAR_FILE=build/libs/FinalprojectMapServer.jar

COPY ${JAR_FILE} /FinalprojectMapServer.jar

ENTRYPOINT  ["java", "-jar", "FinalprojectMapServer.jar"]