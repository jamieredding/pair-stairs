package dev.coldhands.pair.stairs.legacy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.extension.*;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class LoggingExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
    final Logger LOGGER = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

    @Override
    public void beforeEach(ExtensionContext context) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();

        appender.start();
        LOGGER.addAppender(appender);

        getStore(context).put(ListAppender.class, appender);
        getStore(context).put(Level.class, LOGGER.getLevel());
        getStore(context).put(Logger.class, LOGGER);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        var appender = getAppender(context);
        var logLevelBeforeTest = (Level) getStore(context).get(Level.class);

        LOGGER.setLevel(logLevelBeforeTest);
        LOGGER.detachAppender(appender);
        appender.stop();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();

        return type == ListAppender.class || type == Logger.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();

        return getStore(extensionContext).get(type);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    @SuppressWarnings("unchecked")
    private ListAppender<ILoggingEvent> getAppender(ExtensionContext extensionContext) {
        return (ListAppender<ILoggingEvent>) getStore(extensionContext).get(ListAppender.class);
    }
}
