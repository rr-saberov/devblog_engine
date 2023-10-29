FROM openjdk:18
WORKDIR /opt/app
COPY build/libs/engine-1.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]