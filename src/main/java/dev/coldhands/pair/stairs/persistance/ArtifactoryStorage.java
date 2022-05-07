package dev.coldhands.pair.stairs.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.NoSuchFileException;
import java.util.Base64;
import java.util.Map;

import static dev.coldhands.pair.stairs.persistance.ConfigurationUtils.objectMapper;
import static java.net.http.HttpRequest.BodyPublishers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ArtifactoryStorage implements Storage {

    private final ObjectMapper objectMapper = objectMapper();
    private final String fileUrl;
    private final HttpClient httpClient;
    private String username;
    private String password;

    public ArtifactoryStorage(String fileUrl, Map<String, String> environment) {
        this.fileUrl = fileUrl;
        username = environment.get("ARTIFACTORY_USERNAME");
        password = environment.get("ARTIFACTORY_PASSWORD");
        httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void write(Configuration pairings) throws Exception {
        httpClient.send(basicAuthRequest()
                        .PUT(ofByteArray(serialise(pairings)))
                        .uri(URI.create(fileUrl)).build(),
                discarding());
    }

    @Override
    public Configuration read() throws Exception {
        HttpResponse<InputStream> response = httpClient.send(basicAuthRequest()
                        .GET()
                        .uri(URI.create(fileUrl))
                        .build(),
                ofInputStream());

        if (response.statusCode() == 404) {
            throw new NoSuchFileException(fileUrl);
        }

        return objectMapper.readValue(response.body(), Configuration.class);
    }

    @Override
    public String describe() {
        return "Artifactory -> " + fileUrl;
    }

    private byte[] serialise(Configuration configuration) {
        try {
            return objectMapper.writeValueAsBytes(configuration);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpRequest.Builder basicAuthRequest() {
        final String base64 = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(UTF_8));
        return HttpRequest.newBuilder()
                .header("Authorization", "Basic " + base64);
    }
}
