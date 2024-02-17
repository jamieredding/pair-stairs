package dev.coldhands.pair.stairs.legacy.cli;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.coldhands.pair.stairs.legacy.persistance.ArtifactoryStorage;
import dev.coldhands.pair.stairs.legacy.persistance.Configuration;
import dev.coldhands.pair.stairs.legacy.persistance.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;

@WireMockTest
class RunnerWithArtifactoryStorageTest implements RunnerContractTest {

    private CommandLine underTest;
    private Storage storage;

    private static final String FILE_PATH = "/upload/path/config.json";
    private static final int HIGHEST_PRIORITY = 1;
    private String uploadLocation;

    @BeforeEach
    void setUp(StdIO stdIO, WireMockRuntimeInfo wireMockRuntimeInfo) {
        underTest = Runner.createCommandLine(stdIO.in(), stdIO.outWriter(), stdIO.errWriter(), Map.of(
                "ARTIFACTORY_USERNAME", "username",
                "ARTIFACTORY_PASSWORD", "password"));
        underTest.setOut(stdIO.outWriter());
        underTest.setErr(stdIO.errWriter());

        storage = new ArtifactoryStorage(
                wireMockRuntimeInfo.getHttpBaseUrl() + FILE_PATH,
                Map.of(
                        "ARTIFACTORY_USERNAME", "username",
                        "ARTIFACTORY_PASSWORD", "password"));
        stubFor(put(FILE_PATH)
                .atPriority(HIGHEST_PRIORITY + 1)
                .withBasicAuth("username", "password")
                .willReturn(aResponse()
                        .withStatus(200)));
        uploadLocation = wireMockRuntimeInfo.getHttpBaseUrl() + FILE_PATH;
    }

    @AfterEach
    void tearDown() {
        WireMock.reset();
    }

    @Override
    public void persistConfiguration(Configuration pairings) throws Exception {
        storage.write(pairings);

        writePersistedData(readPersistedData());
    }

    public void writePersistedData(String data) {
        stubFor(get(FILE_PATH)
                .atPriority(HIGHEST_PRIORITY)
                .withBasicAuth("username", "password")
                .willReturn(aResponse()
                        .withBody(data)));
    }

    @Override
    public String readPersistedData() {
        return WireMock.findAll(allRequests()).stream()
                .sorted(Comparator.comparing(LoggedRequest::getLoggedDate)
                        .reversed())
                .map(LoggedRequest::getBodyAsString)
                .findFirst()
                .get();
    }

    @Override
    public int executeUnderTest(String... args) {
        String[] actualArgs = Arrays.copyOf(args, args.length + 2);
        actualArgs[actualArgs.length - 2] = "-a";
        actualArgs[actualArgs.length - 1] = uploadLocation;

        return underTest.execute(actualArgs);
    }

    @Override
    public CommandLine underTest() {
        return underTest;
    }

    @Override
    public Storage storage() {
        return storage;
    }
}