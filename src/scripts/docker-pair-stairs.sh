#!/bin/bash

DIRECTORY_OF_SCRIPT="${0%/*}"
ABSOLUTE_PATH=$(readlink -f "${DIRECTORY_OF_SCRIPT}")
DATA_DIR="${ABSOLUTE_PATH}/data"
ARGS=$@

PUID="$(id -u)"
PGID="$(id -g)"

mkdir -p "${DATA_DIR}"

docker run \
  --rm \
  -i \
  --name "pair-stairs" \
  -v "${ABSOLUTE_PATH}/pair-stairs-${project.version}.jar":/opt/pair-stairs.jar \
  -v "${DATA_DIR}":/opt/data \
  eclipse-temurin:17-jre-alpine \
  sh -c "
  addgroup pairs --gid $PGID && \
  adduser pairs --uid $PUID -G pairs --disabled-password && \
  cd /opt && \
  su pairs -c \"java -jar pair-stairs.jar $ARGS\""