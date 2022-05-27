# pair-stairs

This is a cli application to calculate pairing combinations
for the day.

## Usage

Download the latest version from [releases](https://github.com/jamieredding/pair-stairs/releases/latest).

See `--help` for available options.

### Option 1: Docker

There is a [`docker-pair-stairs.sh`](src/scripts/docker-pair-stairs.sh) script that you can run and pass your arguments to directly.

This script will create a directory next to the script and mount this into the container.
This allows you to use file storage and still access the config file that is created.

Note: you will need to specify the path to the config file relative to where the jar file is mounted.

Eg.
```shell
./docker-pair-stairs.sh -f data/config.json -d dev-a dev-b dev-c dev-d
```

### Option 2: Non-Docker

Ensure the `java` in your current shell is Java 17 or higher.

Use [`pair-stairs.sh`](src/scripts/pair-stairs.sh) or update it to pass some of your default options in.

Eg.
```shell
./pair-stairs.sh -f config.json -d dev-a dev-b dev-c dev-d
```

## Building the project

The basic requirements to build are:
- Java 17
    - This can be downloaded and managed with [SDKMAN](https://sdkman.io/install)
- Maven
    - This can be downloaded and managed with the included Maven wrapper script

Once SDKMAN has been installed, run `sdk env` to download and initialise your environment with the same
version of Java used in the project.

You should now be able to build the project using `./mvnw clean verify`.