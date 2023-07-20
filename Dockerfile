#FROM ubuntu:22.04
FROM gradle:latest AS BUILD
RUN chmod +x gradlew
RUN ./gradlew build

