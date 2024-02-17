package dev.coldhands.pair.stairs.cli;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.io.*;

class StdIOExtension extends TypeBasedParameterResolver<StdIO> implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        final StringWriter out = new StringWriter();
        final StringWriter err = new StringWriter();

        var outWriter = new PrintWriter(out);
        var errWriter = new PrintWriter(err);
        var input = new PipedInputStream();
        var output = new PipedOutputStream(input);
        OutputStreamWriter inputWriter = new OutputStreamWriter(output);

        final StdIO stdIO = new StdIO(out, outWriter, err, errWriter, input, inputWriter);
        getStore(extensionContext).put(StdIO.class, stdIO);
    }

    @Override
    public StdIO resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (StdIO) getStore(extensionContext).get(StdIO.class);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
}
