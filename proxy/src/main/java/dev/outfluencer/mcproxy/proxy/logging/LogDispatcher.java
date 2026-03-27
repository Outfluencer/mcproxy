package dev.outfluencer.mcproxy.proxy.logging;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogRecord;

public class LogDispatcher extends Thread {

    private final LinkedBlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>();
    private final ColorLogFormatter consoleFormatter = new ColorLogFormatter(true);
    private final ColorLogFormatter fileFormatter = new ColorLogFormatter(false);
    private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final PrintStream consoleOut;
    private final Path logsDir;

    private PrintStream fileOut;
    private String currentDate;

    public LogDispatcher(PrintStream consoleOut, Path logsDir) {
        super("Logger Thread");
        this.consoleOut = consoleOut;
        this.logsDir = logsDir;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                LogRecord record = queue.take();
                printRecord(record);
            } catch (InterruptedException e) {
                break;
            }
        }
        // drain remaining records on shutdown
        LogRecord record;
        while ((record = queue.poll()) != null) {
            printRecord(record);
        }
        if (fileOut != null) {
            fileOut.close();
        }
    }

    private void printRecord(LogRecord record) {
        consoleOut.print(consoleFormatter.format(record));
        try {
            rotateIfNeeded();
            fileOut.print(fileFormatter.format(record));
            fileOut.flush();
        } catch (IOException e) {
            consoleOut.println("Failed to write to log file: " + e.getMessage());
        }
    }

    private void rotateIfNeeded() throws IOException {
        String today = fileDateFormat.format(new Date());
        if (!today.equals(currentDate)) {
            if (fileOut != null) {
                fileOut.close();
            }
            Files.createDirectories(logsDir);
            Path logFile = logsDir.resolve(today + ".log");
            fileOut = new PrintStream(new FileOutputStream(logFile.toFile(), true));
            currentDate = today;
        }
    }

    public void queue(LogRecord record) {
        if (!isInterrupted()) {
            queue.add(record);
        }
    }
}
