FROM openjdk:21
VOLUME /tmp
ADD target/PaymentService-0.0.1-SNAPSHOT.jar PaymentService.jar
ENTRYPOINT ["java","-jar","/PaymentService.jar"]
