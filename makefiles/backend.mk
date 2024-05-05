DB_CONTAINER_NAME=build_image__pair_stairs_db
BACKEND_IMAGE_NAME=ghcr.io/jamieredding/pair-stairs-backend
TIMEOUT=120
SLEEP=5

.PHONY: start-database wait-for-database run-db build-maven stop-database maven-release push-image-backend

run-db: start-database wait-for-database

build-backend: run-db build-maven stop-database

start-database:
	@echo "Starting MySQL database container..."
	@docker run --rm -d --name $(DB_CONTAINER_NAME) \
	-e MYSQL_DATABASE=pair_stairs \
	-e MYSQL_USER=pair_stairs_user \
	-e MYSQL_PASSWORD=some-password \
	-e MYSQL_ROOT_PASSWORD=some-root-password \
	-p 3306:3306 \
	mysql:8.3.0 || (echo "Failed to start MySQL container." ; exit 1)


wait-for-database:
	@echo "Waiting for MySQL to be ready..."
	@timeout=$(TIMEOUT); \
	while ! docker logs $(DB_CONTAINER_NAME) 2>&1 | grep "/usr/sbin/mysqld: ready for connections" | grep "port: 3306" > /dev/null; do \
		if [ $$timeout -eq 0 ]; then \
			echo "Database did not become ready in $(TIMEOUT) seconds."; \
			exit 1; \
		fi; \
		echo "Waiting for database..."; \
		sleep $(SLEEP); \
		timeout=$$((timeout - 1)); \
	done
	@echo "MySQL database is ready!"

build-maven:
	@echo "Running Maven build..."
	@$(MVN) clean verify || (echo "Maven build failed." ; exit 1)

stop-database:
	@echo "Stopping MySQL database container..."
	@(docker stop $(DB_CONTAINER_NAME) && echo "Stopped.") || echo "No container to stop."

maven-release: check-vars
	@echo "Running Maven release process"
	mvn --batch-mode -DreleaseVersion=$(RELEASE_VERSION) -DdevelopmentVersion=$(DEVELOPMENT_VERSION) release:clean release:prepare

push-image-backend: check-vars
	@echo "Pushing backend docker image"
	docker push $(BACKEND_IMAGE_NAME):latest
	docker rmi -f $(BACKEND_IMAGE_NAME):latest
	docker push $(BACKEND_IMAGE_NAME):$(RELEASE_VERSION)
	docker rmi -f $(BACKEND_IMAGE_NAME):$(RELEASE_VERSION)