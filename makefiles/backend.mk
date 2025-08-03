DB_CONTAINER_NAME=build_image__pair_stairs_db
DATABASE_IMAGE_NAME=mysql:8.4.6
MYSQL_HOST?=$(DB_CONTAINER_NAME)
MYSQL_USER?=pair_stairs_user
MYSQL_PASSWORD?=some-password
MYSQL_ROOT_PASSWORD?=some-root-password
MYSQL_NETWORK?=default
MYSQL_PORT?=3306
DUMP_FILE_PATH?=./all-databases.sql

BACKEND_IMAGE_NAME=ghcr.io/jamieredding/pair-stairs-backend

.PHONY: start-database dump-database restore-database dump-docker restore-docker wait-for-database run-db build-maven stop-database maven-release push-image-backend

run-db: start-database wait-for-database

build-backend: run-db build-maven stop-database

start-database:
	@echo "Starting MySQL database container..."
	@docker run --rm -d --name $(DB_CONTAINER_NAME) \
	-e MYSQL_DATABASE=pair_stairs \
	-e MYSQL_USER=$(MYSQL_USER) \
	-e MYSQL_PASSWORD=$(MYSQL_PASSWORD) \
	-e MYSQL_ROOT_PASSWORD=$(MYSQL_ROOT_PASSWORD) \
	-p 3306:3306 \
	$(DATABASE_IMAGE_NAME) || (echo "Failed to start MySQL container." ; exit 1)


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

dump-docker:
	$(MAKE) dump-database MYSQL_HOST=docker-pair_stairs_db-1 MYSQL_NETWORK=docker_pair_stairs_net

dump-database:
	@echo "Dumping all databases from host $(MYSQL_HOST) on port $(MYSQL_PORT)..."
	@docker run --rm --network $(MYSQL_NETWORK) $(DATABASE_IMAGE_NAME) sh -c '\
		if mysqldump --all-databases -h$(MYSQL_HOST) -P$(MYSQL_PORT) -uroot -p"$(MYSQL_ROOT_PASSWORD)"; then \
			echo "Dump successful" >&2; \
		else \
			echo "Dump failed" >&2; \
			exit 1; \
		fi' > $(DUMP_FILE_PATH) || { echo "Error: Failed to dump databases from host" >&2; exit 1; }

restore-docker:
	$(MAKE) restore-database MYSQL_HOST=docker-pair_stairs_db-1 MYSQL_NETWORK=docker_pair_stairs_net

restore-database:
	@echo "Restoring all databases to host $(MYSQL_HOST) on port $(MYSQL_PORT)..."
	@docker run --rm --network $(MYSQL_NETWORK) -i $(DATABASE_IMAGE_NAME) sh -c '\
		if mysql -h$(MYSQL_HOST) -P$(MYSQL_PORT) -uroot -p"$(MYSQL_ROOT_PASSWORD)"; then \
			echo "Restore successful" >&2; \
		else \
			echo "Restore failed" >&2; \
			exit 1; \
		fi' < $(DUMP_FILE_PATH) || { echo "Error: Failed to restore databases to host" >&2; exit 1; }


build-maven:
	@echo "Running Maven build..."
	@$(MVN) clean verify || (echo "Maven build failed." ; exit 1)

stop-database:
	@echo "Stopping MySQL database container..."
	@(docker stop $(DB_CONTAINER_NAME) && echo "Stopped.") || echo "No container to stop."

maven-release: check-vars run-db
	@echo "Running Maven release process"
	@$(MVN) --batch-mode -DreleaseVersion=$(RELEASE_VERSION) -DdevelopmentVersion=$(DEVELOPMENT_VERSION) release:clean release:prepare

push-image-backend: check-vars
	@echo "Pushing backend docker image"
	docker push $(BACKEND_IMAGE_NAME):latest
	docker rmi -f $(BACKEND_IMAGE_NAME):latest
	docker push $(BACKEND_IMAGE_NAME):$(RELEASE_VERSION)
	docker rmi -f $(BACKEND_IMAGE_NAME):$(RELEASE_VERSION)