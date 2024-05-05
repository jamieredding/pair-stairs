LEGACY_IMAGE_NAME=ghcr.io/jamieredding/pair-stairs

.PHONY: push-image-legacy

push-image-legacy: check-vars
	@echo "Pushing legacy docker image"
	docker push $(LEGACY_IMAGE_NAME):latest
	docker rmi -f $(LEGACY_IMAGE_NAME):latest
	docker push $(LEGACY_IMAGE_NAME):$(RELEASE_VERSION)
	docker rmi -f $(LEGACY_IMAGE_NAME):$(RELEASE_VERSION)