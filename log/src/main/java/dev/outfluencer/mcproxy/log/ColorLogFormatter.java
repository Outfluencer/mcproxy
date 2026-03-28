package dev.outfluencer.mcproxy.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ColorLogFormatter extends Formatter {

    private static final String RESET = "\033[0m";
    private static final String BOLD_RED = "\033[1;31m";
    private static final String RED = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String GREEN = "\033[32m";
    private static final String CYAN = "\033[36m";
    private static final String BLUE = "\033[34m";
    private static final String DARK_GRAY = "\033[90m";
    private static final String WHITE = "\033[97m";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final boolean colors;

    public ColorLogFormatter(boolean colors) {
        this.colors = colors;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        String time = dateFormat.format(new Date(record.getMillis()));
        String levelName = getLevelName(record.getLevel());

        String levelColor = colors ? getLevelColor(record.getLevel()) : "";
        String gray = colors ? DARK_GRAY : "";
        String white = colors ? WHITE : "";
        String reset = colors ? RESET : "";
        String red = colors ? RED : "";

        sb.append(gray).append('[')
                .append(white).append(time)
                .append(' ').append(levelColor).append(levelName)
                .append(gray).append(']')
                .append(' ').append(reset).append(formatMessage(record))
                .append(reset).append('\n');

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append(red).append(sw).append(reset);
        }

        return sb.toString();
    }

    private String getLevelColor(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) return BOLD_RED;
        if (value >= Level.WARNING.intValue()) return YELLOW;
        if (value >= Level.INFO.intValue()) return GREEN;
        if (value >= Level.CONFIG.intValue()) return CYAN;
        if (value >= Level.FINE.intValue()) return BLUE;
        return DARK_GRAY;
    }

    private String getLevelName(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) return "ERROR";
        if (value >= Level.WARNING.intValue()) return "WARN";
        if (value >= Level.INFO.intValue()) return "INFO";
        if (value >= Level.CONFIG.intValue()) return "CONFIG";
        if (value >= Level.FINE.intValue()) return "FINE";
        if (value >= Level.FINER.intValue()) return "FINER";
        return "TRACE";
    }
}
