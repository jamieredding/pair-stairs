FROM eclipse-temurin:25-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs"

ARG DISTRIBUTION_DIR

COPY target/${DISTRIBUTION_DIR} /opt/pair-stairs

WORKDIR /opt

ENTRYPOINT ["pair-stairs/bin/pair-stairs.sh"]