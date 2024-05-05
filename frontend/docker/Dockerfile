FROM nginx:alpine

LABEL org.opencontainers.image.source="https://github.com/jamieredding/pair-stairs"

COPY out /usr/share/nginx/html

COPY docker/nginx.conf.template /etc/nginx/templates/default.conf.template