#!/bin/bash

DIRECTORY_OF_SCRIPT="${0%/*}"
ABSOLUTE_PATH=$(readlink -f "${DIRECTORY_OF_SCRIPT}")
DATA_DIR="${ABSOLUTE_PATH}/data"

mkdir -p "${DATA_DIR}"

docker run \
  -i \
  --rm \
  --name "pair-stairs" \
  -v "${DATA_DIR}":/opt/data \
  --user "$(id -u):$(id -g)" \
  ghcr.io/jamieredding/pair-stairs:latest \
  "$@"