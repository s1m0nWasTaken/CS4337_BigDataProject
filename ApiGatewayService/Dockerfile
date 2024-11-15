FROM openjdk:21-jdk-slim

COPY /build/libs/ApiGatewayService-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT [ "java" , "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" , "-jar" , "app.jar"]
EXPOSE ${API_GATEWAY_CONTAINER_PORT}