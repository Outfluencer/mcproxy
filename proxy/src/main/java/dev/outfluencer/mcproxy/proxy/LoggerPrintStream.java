package dev.outfluencer.mcproxy.proxy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class LoggerPrintStream extends PrintStream {

    private final Logger logger;
    private final Level level;

    LoggerPrintStream(Logger logger, Level level) {
        super(new ByteArrayOutputStream(), true);
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void println(String message) {
        logger.log(level, message);
    }

    @Override
    public void println(Object object) {
        logger.log(level, String.valueOf(object));
    }

    @Override
    public void println() {
        logger.log(level, "");
    }

    @Override
    public void print(String message) {
        logger.log(level, message);
    }

    @Override
    public void print(Object object) {
        logger.log(level, String.valueOf(object));
    }
}
