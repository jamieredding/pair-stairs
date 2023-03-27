FROM eclipse-temurin:20-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs"

COPY target/pair-stairs.jar /opt/pair-stairs.jar

WORKDIR /opt

ENTRYPOINT ["java", "-jar", "pair-stairs.jar"]