#!/usr/bin/env bash

function main() {
  docker run -it --rm \
    -p 18085:8080 \
    --name wiremock \
    -v "$PWD":/home/wiremock \
    wiremock/wiremock:3.5.2 --verbose
}

main "$@"