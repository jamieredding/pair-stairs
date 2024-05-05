MVN=./mvnw
VERSION ?= $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)

.PHONY: build release git-push

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

build: build-backend build-frontend

release: check-vars frontend-release maven-release prepare-new-iteration-frontend git-push
