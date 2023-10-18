FROM openjdk:12-jdk-alpine

ARG JAR_FILE

RUN addgroup -S user && adduser -S user -G user

USER user:user
COPY ${JAR_FILE} app.jar
CMD ["java","-jar","app.jar"]