MVN=./mvnw
VERSION ?= $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)
TIMEOUT=120
SLEEP=5
PUSH_CHANGES?=true

.PHONY: build release git-push push-images teardown

check-vars:
ifndef RELEASE_VERSION
	$(error RELEASE_VERSION is undefined. Set this variable to continue.)
endif
ifndef DEVELOPMENT_VERSION
	$(error DEVELOPMENT_VERSION is undefined. Set this variable to continue.)
endif

git-push:
	@if $(PUSH_CHANGES); then \
		 git push --tags; \
		 else \
		 echo "Push disabled, but would have pushed"; \
	 fi

include makefiles/backend.mk
include makefiles/frontend.mk
include makefiles/legacy.mk
include makefiles/e2e.mk
include makefiles/docker.mk

build: run-maven-build run-all-e2e-suites

release: check-vars prepare-release-frontend maven-release stop-database prepare-new-iteration-frontend git-push

push-images: push-image-legacy push-image-web

teardown: stop-database teardown-e2e-suites
