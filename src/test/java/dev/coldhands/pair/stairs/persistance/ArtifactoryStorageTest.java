package dev.coldhands.pair.stairs.persistance;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;

@WireMockTest
class ArtifactoryStorageTest implements StorageContractTest {

    private static final String FILE_PATH = "/upload/path/config.json";
    private static final int HIGHEST_PRIORITY = 1;
    private Storage underTest;
    private String uploadLocation;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
        underTest = new ArtifactoryStorage(
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
    public Storage underTest() {
        return underTest;
    }

    @Override
    public String readPersistedData() {
        return WireMock.findAll(allRequests()).stream()
                .map(LoggedRequest::getBodyAsString)
                .findFirst()
                .get();
    }

    @Override
    public void writePersistedData(String data) {
        stubFor(get(FILE_PATH)
                .atPriority(HIGHEST_PRIORITY)
                .withBasicAuth("username", "password")
                .willReturn(aResponse()
                        .withBody(data)));
    }

    @Override
    public String storageDescription() {
        return "Artifactory -> " + uploadLocation;
    }
}
