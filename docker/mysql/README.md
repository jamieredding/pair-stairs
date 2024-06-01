# mysql

This stack is for having the backend data storage being backed by a mysql container.

## Extra containers
- pair-stairs-db
  - This container runs the mysql database for pair-stairs
  - It uses the official mysql image and is configured using the environment variables in the [application.env](./application.env) file

## Running the containers

To run the containers, there is a provided [docker-compose.yml](./docker-compose.yml) file in this directory.

To start the containers, run the following command:
```shell
docker compose --env-file environment/prod.env up
```

To stop the containers, run the following command (include -v to also remove the volumes):
```shell
docker compose --env-file environment/prod.env down
```

### Configuration

Environment variables for the database and backend server are provided in the [application.env](./application.env) file.

#### Backend configuration
If you require further customisation, you can mount your own [application.properties](/backend/src/main/resources/application.properties)
file to `/opt/application.properties` to override the defaults.

If you don't need to override all of the properties, consider using spring-boot's environment 
variable binding functionality ([link](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables)).

E.g. `-e SPRING_DATASOURCE_URL=xxx` will convert to a property of `spring.datasource.url=xxx`.

#### Docker compose
Separately, there are environment variables for the `docker-compose.yml` file itself in the [environment](./environment) directory.
These are for running the stack in different environments:
- [prod.env](./environment/prod.env)
  - This is the default environment for running the stack
- [e2e.env](./environment/e2e.env)
  - This is the environment for running the stack in an end-to-end testing environment
  - Primarily used for running the full-stack playwright tests

To run both environments simultaneously, please ensure you specify the `--project-name` flag to add a unique namespace for all services, volumes, and networks:
```shell
docker compose --env-file environment/prod.env --project-name prod up -d
docker compose --env-file environment/e2e.env --project-name e2e up -d
```