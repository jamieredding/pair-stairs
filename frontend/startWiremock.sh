#!/usr/bin/env bash

docker run -it --rm \
  -p 18082:8080 \
  --name wiremock \
  -v "$PWD/wiremock":/home/wiremock \
  wiremock/wiremock:3.5.2 --verbose