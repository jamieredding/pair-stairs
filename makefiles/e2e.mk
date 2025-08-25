PLAYWRIGHT_CONTAINER_NAME=build_image__playwright
PLAYWRIGHT_IMAGE=mcr.microsoft.com/playwright:v1.44.0-jammy
E2E_WEB_CONTAINER_NAME=e2e-pair_stairs_web-1
E2E_DOCKER_NETWORK=e2e_pair_stairs_net
MYSQL_DOCKER_COMPOSE_ROOT=docker/mysql
H2_DOCKER_COMPOSE_ROOT=docker/h2

.PHONY: run-e2e-suite run-e2e-tests start-e2e-pair-stairs stop-e2e-pair-stairs run-e2e-pair-stairs run-all-e2e-suites run-e2e-suite-h2 run-e2e-suite-mysql teardown-e2e-suites teardown-e2e-suite-mysql teardown-e2e-suite-h2 restart-dev-e2e-pair-stairs

run-e2e-suite-mysql:
	@$(MAKE) run-e2e-suite COMPOSE_PATH=$(MYSQL_DOCKER_COMPOSE_ROOT)

run-e2e-suite-h2:
	@$(MAKE) run-e2e-suite COMPOSE_PATH=$(H2_DOCKER_COMPOSE_ROOT)

run-all-e2e-suites: run-e2e-suite-h2 run-e2e-suite-mysql

run-e2e-suite: run-e2e-pair-stairs run-e2e-tests stop-e2e-pair-stairs

teardown-e2e-suite-mysql:
	@$(MAKE) stop-e2e-pair-stairs COMPOSE_PATH=$(MYSQL_DOCKER_COMPOSE_ROOT)

teardown-e2e-suite-h2:
	@$(MAKE) stop-e2e-pair-stairs COMPOSE_PATH=$(H2_DOCKER_COMPOSE_ROOT)

teardown-e2e-suites: teardown-e2e-suite-mysql teardown-e2e-suite-h2

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

run-e2e-pair-stairs: start-e2e-pair-stairs

start-e2e-pair-stairs:
	@echo "Starting e2e pair stairs... ($(COMPOSE_PATH))"
	@cd $(COMPOSE_PATH) && docker compose --env-file environment/e2e.env --project-name e2e up -d --wait --wait-timeout 60 || (echo "Failed to start e2e pair stairs." ; exit 1)

stop-e2e-pair-stairs:
	@echo "Stopping e2e pair stairs... ($(COMPOSE_PATH))"
	@cd $(COMPOSE_PATH) && docker compose --env-file environment/e2e.env --project-name e2e down -v || echo "Failed to stop e2e pair stairs."

start-dev-e2e-pair-stairs:
	@echo "Starting e2e pair stairs... ($(COMPOSE_PATH))"
	@cd $(COMPOSE_PATH) && docker compose --env-file environment/e2e_dev.env --project-name e2e up -d --wait --wait-timeout 60 || (echo "Failed to start e2e pair stairs." ; exit 1)

stop-dev-e2e-pair-stairs:
	@echo "Stopping e2e pair stairs... ($(COMPOSE_PATH))"
	@cd $(COMPOSE_PATH) && docker compose --env-file environment/e2e_dev.env --project-name e2e down -v || echo "Failed to stop e2e pair stairs."

restart-dev-e2e-pair-stairs: stop-dev-e2e-pair-stairs start-dev-e2e-pair-stairs
