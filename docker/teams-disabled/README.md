# teams-disabled

This stack is for running without teams and login support.
Primarily this is for backwards compatibility support while teams support is under development.

## Running the containers

To run the containers, there is a provided [docker-compose.yml](./docker-compose.yml) file in this directory.

To start the containers, run the following command:
```shell
docker compose --env-file environment/prod.env up --wait --wait-timeout 60
```

To stop the containers, run the following command (include -v to also remove the volumes):
```shell
docker compose --env-file environment/prod.env down
```

### Configuration

#### Backend configuration
You can mount your own [application.properties](/backend/src/main/resources/application.properties)
file to `/opt/application.properties` to override the defaults.

If you don't need to override all the properties, consider using spring-boot's environment 
variable binding functionality ([link](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables)).

E.g. `-e SPRING_DATASOURCE_URL=xxx` will convert to a property of `spring.datasource.url=xxx`.

#### Docker compose
Separately, there are environment variables for the `docker-compose.yml` file itself in the [environment](./environment) directory.
These are for running the stack in different environments:
- [prod.env](./environment/prod.env)
  - This is the default environment for running the stack
- [e2e_dev.env](environment/e2e_dev.env)
  - Used during development of the e2e tests
  - Not required but kept due to naming convention
- [e2e_ci.env](environment/e2e_ci.env)
  - Used during CI/running the e2e tests directly using make
  - Not required but kept due to naming convention