FROM openjdk:17-jdk-slim

MAINTAINER xiaou

WORKDIR /app

COPY target/u-space-3.X-order-service.jar app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar", "--spring.profiles.active=dev"] 