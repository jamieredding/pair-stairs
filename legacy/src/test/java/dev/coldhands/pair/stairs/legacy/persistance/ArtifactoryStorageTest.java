package dev.coldhands.pair.stairs.legacy.persistance;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.coldhands.pair.stairs.legacy.LoggingExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
@ExtendWith(LoggingExtension.class)
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
        return STR."Artifactory -> \{uploadLocation}";
    }

    @Test
    void readFromArtifactoryHttpRequestsAreLoggedWhenDebugEnabled(WireMockRuntimeInfo wireMockRuntimeInfo,
                                                                  ListAppender<ILoggingEvent> appender) throws Exception {
        Configuration configuration = new Configuration(List.of(), List.of());

        underTest().write(configuration);
        String persistedData = readPersistedData();
        writePersistedData(persistedData);

        Logger logger = (Logger) LoggerFactory.getLogger(ArtifactoryStorage.class);
        logger.setLevel(Level.DEBUG);

        underTest.read();

        assertThat(appender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(event.getLoggerName()).isEqualTo(ArtifactoryStorage.class.getName());
                    assertThat(event.getFormattedMessage()).isEqualTo(STR."Request: GET \{wireMockRuntimeInfo.getHttpBaseUrl()}\{FILE_PATH}");
                })
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(event.getLoggerName()).isEqualTo(ArtifactoryStorage.class.getName());
                    assertThat(event.getFormattedMessage()).isEqualTo(STR."Response: [200] \{persistedData}");
                });
    }

    @Test
    void writeFromArtifactoryHttpRequestsAreLoggedWhenDebugEnabled(WireMockRuntimeInfo wireMockRuntimeInfo,
                                                                   ListAppender<ILoggingEvent> appender) throws Exception {
        Configuration configuration = new Configuration(List.of(), List.of());


        Logger logger = (Logger) LoggerFactory.getLogger(ArtifactoryStorage.class);
        logger.setLevel(Level.DEBUG);

        underTest().write(configuration);

        assertThat(appender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(event.getLoggerName()).isEqualTo(ArtifactoryStorage.class.getName());
                    assertThat(event.getFormattedMessage()).isEqualTo(STR."Request: PUT \{wireMockRuntimeInfo.getHttpBaseUrl()}\{FILE_PATH}");
                })
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
                    assertThat(event.getLoggerName()).isEqualTo(ArtifactoryStorage.class.getName());
                    assertThat(event.getFormattedMessage()).isEqualTo("Response: [200]");
                });
    }
}
