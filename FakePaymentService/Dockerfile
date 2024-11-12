FROM openjdk:21-jdk-slim

COPY /build/libs/FakePaymentService-1.0.0-plain.jar app.jar

ENTRYPOINT [ "java" , "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" , "-jar" , "app.jar"]
EXPOSE ${PAYMENT_CONTAINER_PORT}
