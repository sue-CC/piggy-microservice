FROM openjdk:17-jdk-alpine
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/account-service-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/account-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 9090