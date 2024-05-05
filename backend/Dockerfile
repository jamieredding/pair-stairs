FROM eclipse-temurin:22-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs-backend"

ARG JAR_FILE

COPY target/${JAR_FILE} /opt/backend.jar

WORKDIR /opt

ENTRYPOINT ["java", "--enable-preview", "-jar", "backend.jar"]