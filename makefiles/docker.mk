WEB_CONTAINER_NAME=docker-pair_stairs_web-1
DOCKER_NETWORK=docker_pair_stairs_net

.PHONY: run-stack start-stack wait-for-pair-stairs stop-stack stop-stack-and-volume

run-stack: start-stack wait-for-pair-stairs

start-stack:
	@echo "Starting pair stairs..."
	@cd docker && docker compose --env-file environment/prod.env up -d || (echo "Failed to start pair stairs." ; exit 1)

stop-stack:
	@echo "Stopping pair stairs..."
	@cd docker && docker compose --env-file environment/prod.env down || echo "Failed to stop pair stairs."

stop-stack-and-volume:
	@echo "Stopping pair stairs (and removing volume)..."
	@cd docker && docker compose --env-file environment/prod.env down -v || echo "Failed to stop pair stairs."

wait-for-pair-stairs:
	@echo "Waiting for backend to be ready..."
	@timeout=$(TIMEOUT); \
	while ! docker logs $(WEB_CONTAINER_NAME) 2>&1 | grep "Started Application in" > /dev/null; do \
		if [ $$timeout -eq 0 ]; then \
			echo "Backend did not become ready in $(TIMEOUT) seconds."; \
			exit 1; \
		fi; \
		echo "Waiting for backend..."; \
		sleep $(SLEEP); \
		timeout=$$((timeout - 1)); \
	done
	@echo "Backend is ready!"
