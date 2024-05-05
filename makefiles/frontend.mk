FRONTEND_IMAGE_NAME=ghcr.io/jamieredding/pair-stairs-frontend

.PHONY: build-frontend build-npm build-frontend-image sync-version-with-frontend prepare-release-frontend prepare-new-iteration-frontend frontend-release push-image-frontend tag-image-with-release-version

build-frontend: build-npm build-frontend-image

build-npm:
	@echo "Running frontend build..."
	@cd frontend && npm install && npm run build || (echo "Frontend build failed." ; exit 1)

build-frontend-image:
	@echo "Building frontend image..."
	@cd frontend && docker build -f docker/Dockerfile -t $(FRONTEND_IMAGE_NAME):latest -t $(FRONTEND_IMAGE_NAME):$(VERSION) . || (echo "Failed to build frontend image." ; exit 1)

sync-version-with-frontend:
	@echo "Synchronising frontend version to $(VERSION)"
	@cd frontend && npm version $(VERSION) --no-git-tag-version

frontend-release: prepare-release-frontend build-frontend tag-image-with-release-version git-push

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

push-image-frontend: check-vars
	@echo "Pushing frontend docker image"
	docker push $(FRONTEND_IMAGE_NAME):latest
	docker rmi -f $(FRONTEND_IMAGE_NAME):latest
	docker push $(FRONTEND_IMAGE_NAME):$(RELEASE_VERSION)
	docker rmi -f $(FRONTEND_IMAGE_NAME):$(RELEASE_VERSION)