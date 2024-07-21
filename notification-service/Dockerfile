FROM openjdk:22-jdk
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/notification-service-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/notification-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 9092
