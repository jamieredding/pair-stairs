# Docker

There are two example stacks to use here, the main difference being how the backend data is stored.

- [h2](./h2/README.md)
  - The backend data is stored in a file that is managed in-memory by the backend container
  - This can be simpler to manage than a full database
- [mysql](./mysql/README.md)
  - The backend data is stored in a mysql database running in its own container
  - This will offer a more scalable solution but required more considered maintenance

## Primary containers

- pair-stairs-backend
  - This container runs the backend server for pair-stairs
    - Handling API requests from the frontend and persisting data to the database
  - It is built from [/backend/Dockerfile](/backend/Dockerfile) using `dockerfile-maven-plugin` in the [/backend/pom.xml](/backend/pom.xml)
- pair-stairs-frontend
  - This container runs the frontend server (nginx) for pair-stairs
    - It serves both the static files and proxies API requests to the backend server
  - It is built from [/frontend/docker/Dockerfile](/frontend/docker/Dockerfile) by the makefile target `build-frontend-image` in [/makefiles/frontend.mk](/makefiles/frontend.mk)

## Running the containers

Please see the relevant README.md ([h2](./h2/README.md), [mysql](./mysql/README.md)) for instructions and more detail.
