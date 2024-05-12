# Docker

There are currently 3 containers required for running pair-stairs:

- pair-stairs-backend
  - This container runs the backend server for pair-stairs
    - Handling API requests from the frontend and persisting data to the database
  - It is built from [backend/Dockerfile](../backend/Dockerfile) using `dockerfile-maven-plugin` in the [backend/pom.xml](../backend/pom.xml)
- pair-stairs-frontend
  - This container runs the frontend server (nginx) for pair-stairs
    - It serves both the static files and proxies API requests to the backend server
  - It is built from [frontend/docker/Dockerfile](../frontend/docker/Dockerfile) by the makefile target `build-frontend-image` in [frontend.mk](../makefiles/frontend.mk)
- pair-stairs-db
  - This container runs the mysql database for pair-stairs
  - It uses the official mysql image and is configured using the environment variables in the [application.env](application.env) file

## Running the containers

To run the containers, there is a provided [docker-compose.yml](docker-compose.yml) file in this directory.

To start the containers, run the following command:
```shell
docker compose --env-file environment/prod.env up
```

To stop the containers, run the following command (include -v to also remove the volumes):
```shell
docker compose --env-file environment/prod.env down
```

### Configuration

Environment variables for the database and backend server are provided in the [application.env](application.env) file.

Separately, there are environment variables for the `docker-compose.yml` file itself in the [environment](environment) directory.
These are for running the stack in different environments:
- [prod.env](environment/prod.env)
  - This is the default environment for running the stack
- [e2e.env](environment/e2e.env)
  - This is the environment for running the stack in an end-to-end testing environment
  - Primarily used for running the full-stack playwright tests

To run both environments simultaneously, please ensure you specify the `--project-name` flag to add a unique namespace for all services, volumes, and networks:
```shell
docker compose --env-file environment/prod.env --project-name prod up -d
docker compose --env-file environment/e2e.env --project-name e2e up -d
```