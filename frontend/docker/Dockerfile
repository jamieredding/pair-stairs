FROM nginx:alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs"

RUN apk --no-cache add apache2-utils

COPY out /usr/share/nginx/html

COPY docker/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY docker/40-configure-basic-auth.sh /docker-entrypoint.d/40-configure-basic-auth.sh
