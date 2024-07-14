FROM openjdk:17-oracle
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/statistics-service-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/statistics-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 7000
EXPOSE 9093