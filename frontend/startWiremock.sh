#!/usr/bin/env bash

function devMode() {
  docker run -it --rm \
    -p 18082:8080 \
    --name wiremock \
    -v "$PWD/wiremock":/home/wiremock \
    wiremock/wiremock:3.13.1 --verbose
}

function integrationMode() {
  docker run -it --rm \
    --name wiremock \
    -v "$PWD/wiremock":/home/wiremock \
    --network manual-pair-stairs \
    wiremock/wiremock:3.13.1 --verbose --port 8081
}

function main() {
  local mode="${1:?What mode do you want? dev or integration}"

  case "${mode}" in
    dev)
      devMode
      ;;
    integration)
      integrationMode
      ;;
    *)
      echo "Usage ${0} {dev|integration}"
      ;;
  esac
}

main "$@"