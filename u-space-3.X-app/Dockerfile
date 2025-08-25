FROM openjdk:17-jdk-slim

MAINTAINER xiaou

WORKDIR /app

COPY target/u-space-3.X-app.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]