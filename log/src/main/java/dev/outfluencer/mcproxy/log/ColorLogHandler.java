package dev.outfluencer.mcproxy.log;

import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ColorLogHandler extends Handler {

    private final LogDispatcher dispatcher;

    public ColorLogHandler() {
        this.dispatcher = new LogDispatcher(System.out, Path.of("logs"));
        this.dispatcher.start();
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        dispatcher.queue(record);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        dispatcher.interrupt();
        try {
            dispatcher.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Installs the ColorLogHandler on the root logger, replacing the default handlers.
     */
    public static void install() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.addHandler(new ColorLogHandler());
    }
}
