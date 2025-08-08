.PHONY: prepare-release-frontend prepare-new-iteration-frontend

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
