PLAYWRIGHT_CONTAINER_NAME=build_image__playwright
PLAYWRIGHT_IMAGE=mcr.microsoft.com/playwright:v1.44.0-jammy
E2E_BACKEND_CONTAINER_NAME=e2e-pair_stairs_backend-1
E2E_DOCKER_NETWORK=e2e_pair_stairs_net

.PHONY: run-e2e-suite run-e2e-tests start-e2e-pair-stairs stop-e2e-pair-stairs wait-for-e2e-pair-stairs run-e2e-pair-stairs

run-e2e-suite: run-e2e-pair-stairs run-e2e-tests stop-e2e-pair-stairs

run-e2e-tests:
	@echo "Running e2e tests..."
	docker run --rm --name $(PLAYWRIGHT_CONTAINER_NAME) \
	--ipc=host \
	--network $(E2E_DOCKER_NETWORK) \
	-v "$(PWD)/e2e:/home/pwuser/e2e" \
	-e CI="yes" \
	--user pwuser --security-opt seccomp="$(PWD)/e2e/seccomp_profile.json" \
	$(PLAYWRIGHT_IMAGE) \
	/bin/bash -c 'cd ~/e2e && npm install && npx playwright test' || (echo "e2e tests have failed" ; exit 1)

run-e2e-pair-stairs: start-e2e-pair-stairs wait-for-e2e-pair-stairs

start-e2e-pair-stairs:
	@echo "Starting e2e pair stairs..."
	@cd docker && docker compose --env-file environment/e2e.env --project-name e2e up -d || (echo "Failed to start e2e pair stairs." ; exit 1)

stop-e2e-pair-stairs:
	@echo "Stopping e2e pair stairs..."
	@cd docker && docker compose --env-file environment/e2e.env --project-name e2e down -v || echo "Failed to stop e2e pair stairs."

wait-for-e2e-pair-stairs:
	@echo "Waiting for backend to be ready..."
	@timeout=$(TIMEOUT); \
	while ! docker logs $(E2E_BACKEND_CONTAINER_NAME) 2>&1 | grep "Started Application in" > /dev/null; do \
		if [ $$timeout -eq 0 ]; then \
			echo "Backend did not become ready in $(TIMEOUT) seconds."; \
			exit 1; \
		fi; \
		echo "Waiting for backend..."; \
		sleep $(SLEEP); \
		timeout=$$((timeout - 1)); \
	done
	@echo "Backend is ready!"
