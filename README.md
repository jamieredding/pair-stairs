# pair-stairs

This is a cli application to calculate pairing combinations
for the day.

## Building the project


The basic requirements to build are:
- Java 17
    - This can be downloaded and managed with [SDKMAN](https://sdkman.io/install)
- Maven
    - This can be downloaded and managed with the included Maven wrapper script

Once SDKMAN has been installed, run `sdk env` to download and initialise your environment with the same
version of Java used in the project.

You should now be able to build the project using `./mvnw clean verify`.