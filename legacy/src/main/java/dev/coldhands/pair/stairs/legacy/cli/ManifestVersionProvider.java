package dev.coldhands.pair.stairs.legacy.cli;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.jar.Manifest;

import static java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION;

class ManifestVersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        ClassLoader classLoader = this.getClass().getClassLoader();

        String version = classLoader.resources("META-INF/MANIFEST.MF")
                .map(url -> {
                    try (final InputStream inputStream = url.openStream()) {
                        return new Manifest(inputStream).getMainAttributes();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .filter(attributes -> Objects.equals(attributes.getValue(IMPLEMENTATION_TITLE), "legacy"))
                .map(attributes -> attributes.getValue(IMPLEMENTATION_VERSION))
                .findFirst()
                .get();

        return new String[]{version};
    }
}
