MVN=./mvnw
VERSION ?= $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)

include makefiles/backend.mk
include makefiles/frontend.mk

.PHONY: build

build: build-backend build-frontend
