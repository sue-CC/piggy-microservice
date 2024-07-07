FROM openjdk:17-oracle
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/notification-service-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/notification-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 8000