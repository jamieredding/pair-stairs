package dev.coldhands.pair.stairs.cli;

import java.nio.file.Path;

class RunnerViaDockerImageIT extends RunnerContractIT {

    private static final String BASE_DIR = "target";
    private static final String DATA_DIR = STR."\{BASE_DIR}/data";
    private static final String PATH_TO_SCRIPT = STR."\{BASE_DIR}/\{PROPERTIES.getProperty("scriptName")}";
    private static final Path CONFIG_FILE_PATH_INSIDE_CONTAINER = Path.of("data/config.json");
    private static final Path CONFIG_FILE_PATH_OUTSIDE_CONTAINER = Path.of(STR."\{DATA_DIR}/config.json");

    @Override
    Path getConfigFilePathArgument() {
        return CONFIG_FILE_PATH_INSIDE_CONTAINER;
    }

    @Override
    Path getConfigFilePath() {
        return CONFIG_FILE_PATH_OUTSIDE_CONTAINER;
    }

    @Override
    String getPathToScript() {
        return PATH_TO_SCRIPT;
    }

}
