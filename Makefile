MVN=./mvnw
VERSION ?= $(shell $(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout)

include makefiles/backend.mk
include makefiles/frontend.mk

.PHONY: build release git-push

build: build-backend build-frontend

release: check-vars prepare-release-frontend maven-release prepare-new-iteration-frontend git-push

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