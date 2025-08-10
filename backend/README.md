# Backend

This is the [Spring Boot](https://spring.io/projects/spring-boot) application that provides the API and persistence layer over the core business logic.

## Development
For local development you will need to bring up the dependencies required for tests to pass locally:
- a mysql database
- an oauth identity provider

You can do this by running:

```shell
cd ..
make start-services
```

Or stop them with:

```shell
cd ..
make stop-services
```