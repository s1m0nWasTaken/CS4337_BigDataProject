FROM openjdk:21-jdk-slim

COPY build/libs/Project-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT [ "java" , "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" , "-jar" , "app.jar"]
