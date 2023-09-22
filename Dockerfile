FROM eclipse-temurin:20-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs"

COPY target/pair-stairs-dist /opt/pair-stairs

WORKDIR /opt

ENTRYPOINT ["pair-stairs/bin/pair-stairs.sh"]