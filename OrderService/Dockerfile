FROM openjdk:21-jdk-slim

COPY /build/libs/OrderService-0.0.1-SNAPSHOT-plain.jar app.jar

ENTRYPOINT [ "java" , "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" , "-jar" , "app.jar"]
EXPOSE ${ORDER_CONTAINER_PORT}
