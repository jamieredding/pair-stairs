FRONTEND_IMAGE_NAME=ghcr.io/jamieredding/pair-stairs-frontend

.PHONY: prepare-release-frontend prepare-new-iteration-frontend frontend-release tag-image-with-release-version

frontend-release: prepare-release-frontend tag-image-with-release-version

prepare-release-frontend: check-vars
	@echo "Updating frontend version to $(RELEASE_VERSION)"
	cd frontend && npm version $(RELEASE_VERSION) --no-git-tag-version
	git add frontend/package.json frontend/package-lock.json
	git commit -m "Update frontend version to $(RELEASE_VERSION)"

prepare-new-iteration-frontend: check-vars
	@echo "Updating frontend version to $(DEVELOPMENT_VERSION)"
	cd frontend && npm version $(DEVELOPMENT_VERSION) --no-git-tag-version
	git add frontend/package.json frontend/package-lock.json
	git commit -m "Update frontend version to $(DEVELOPMENT_VERSION)"
	git push

tag-image-with-release-version: check-vars
	@echo "Tagging image with $(RELEASE_VERSION)"
	docker image tag $(FRONTEND_IMAGE_NAME):latest $(FRONTEND_IMAGE_NAME):$(RELEASE_VERSION)
