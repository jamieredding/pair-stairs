FROM eclipse-temurin:18-jre-alpine

COPY target/pair-stairs.jar /opt/pair-stairs.jar

WORKDIR /opt

ENTRYPOINT ["java", "-jar", "pair-stairs.jar"]