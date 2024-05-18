MVN=./mvnw
VERSION ?= $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
TIMEOUT=120
SLEEP=5

.PHONY: build release git-push push-images teardown

check-vars:
ifndef RELEASE_VERSION
	$(error RELEASE_VERSION is undefined. Set this variable to continue.)
endif
ifndef DEVELOPMENT_VERSION
	$(error DEVELOPMENT_VERSION is undefined. Set this variable to continue.)
endif

git-push:
	@echo "Pushing changes"
	@git push

include makefiles/backend.mk
include makefiles/frontend.mk
include makefiles/legacy.mk
include makefiles/e2e.mk
include makefiles/docker.mk

build: build-backend build-frontend run-e2e-suite

release: check-vars frontend-release maven-release stop-database prepare-new-iteration-frontend git-push

push-images: push-image-legacy push-image-backend push-image-frontend

teardown: stop-database stop-e2e-pair-stairs
