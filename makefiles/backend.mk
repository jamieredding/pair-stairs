DATABASE_IMAGE_NAME=mysql:9.4.0
MYSQL_HOST?=build_image__pair_stairs_db
MYSQL_ROOT_PASSWORD?=some-root-password
MYSQL_NETWORK?=default
MYSQL_PORT?=3306
DUMP_FILE_PATH?=./all-databases.sql

WEB_IMAGE_NAME=ghcr.io/jamieredding/pair-stairs-web

COMPOSE = docker compose --file makefiles/docker-compose.yml --env-file makefiles/.env

.PHONY: start-services dump-database restore-database dump-docker restore-docker build-maven maven-release push-image-web

run-maven-build: start-services build-maven stop-services

start-services:
	$(COMPOSE) up --detach --wait --wait-timeout 60 db oauth

stop-services:
	$(COMPOSE) down --volumes --timeout 10 db oauth

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

maven-release: check-vars start-services
	@echo "Running Maven release process"
	@$(MVN) --batch-mode -DreleaseVersion=$(RELEASE_VERSION) -DdevelopmentVersion=$(DEVELOPMENT_VERSION) -DpushChanges=false release:clean release:prepare

push-image-web: check-vars
	@echo "Pushing web docker image"
	docker push $(WEB_IMAGE_NAME):latest
	docker rmi -f $(WEB_IMAGE_NAME):latest
	docker push $(WEB_IMAGE_NAME):$(RELEASE_VERSION)
	docker rmi -f $(WEB_IMAGE_NAME):$(RELEASE_VERSION)
