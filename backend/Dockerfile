FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs"

ARG JAR_FILE

COPY target/${JAR_FILE} /opt/backend.jar

COPY target/classes/application.properties /opt/application.properties

WORKDIR /opt

ENTRYPOINT ["java", "--enable-preview", "-Dspring.config.location=file:application.properties", "-jar", "backend.jar"]