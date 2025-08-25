# E2E testing

This module uses playwright to run end-to-end tests on the full stack of pair-stairs.

## Running the tests
To run as if in CI (without playwright installed locally), from the root of the project:
```shell
make run-e2e-suite
```

## Developing the tests
### Pre-requisites

1. You will need to have playwright installed locally to run the tests.
2. You will need to start the stack up in e2e mode: `COMPOSE_PATH=docker/h2 make start-dev-e2e-pair-stairs`
   - This can be stopped with `COMPOSE_PATH=docker/h2 make stop-dev-e2e-pair-stairs`
   - The UI will be visible at `http://localhost:28081/`
   - You will need to log in with the default credentials
     - username: `admin@example.com`
     - password: `password`
   - If you'd prefer to run this against a mysql database instead of h2, set `COMPOSE_PATH=docker/mysql` instead

Your normal loop for developing tests will look like this:
- From the [e2e](.) directory
- Run the tests
  - `npx playwright test --ui`
- Use the codegen feature of playwright to help get the rough shape of your test by generating the selectors
  - `npx playwright codegen http://localhost:28081/`
- Clean the database
  - Restart the app: `COMPOSE_PATH=docker/h2 make restart-dev-e2e-pair-stairs`
  - Or run [cleanTables.sql](./cleanTables.sql) to delete all data from the database

## Environment setup
Please see the [playwright documentation](https://playwright.dev/docs/intro) for installing playwright locally.

### Temporary Ubuntu 24.04 setup

Until [this issue](https://github.com/microsoft/playwright/issues/30368) is resolved, you might need to follow these
steps to manually install playwright for your system.

You will only need this for local development of the tests, not for running them.

1. Run `npx playwright install --with-deps`
   - This will install some dependencies but fail to find others:
     - `libicu70`
     - `libffi7`
     - `libx264-163`
2. Search for these packages in [here](https://packages.ubuntu.com/)
   - Make sure you set the `distribution` dropdown to `all` so you can find versions from `22.04`
   - Download the `.deb` files for each package for your architecture (likely `amd64`)
3. Install the packages with `sudo apt install ./<package>.deb`
4. Run `npx playwright install` (this time without `--with-deps`)
   - This should complain about two other missing packages:
     - `libvpx7`
     - `libevent-2.1-7`
   - Repeat steps 2 and 3 for these packages
5. Run `npx playwright install` again
6. You should now have playwright installed
7. Run `npx playwright test` to run the tests