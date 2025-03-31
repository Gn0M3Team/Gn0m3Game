package com.gnome.gnome.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

/**
 * This log is created for the editor page, for testing the function.
 * <p>
 * If you need a logger, you can use this configuration,
 * but <strong>do not change anything without permission</strong>.
 * Write to the original author to request modifications,
 * or feel free to create your own logger setup.
 * <br><br>
 * Thank you for your understanding.
 */
public class EditorLogger {

    // ANSI color codes for console output
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    /**
     * Configures the root Java logger with:
     * <ul>
     *     <li>Colored console output using a custom formatter</li>
     *     <li>File output to {@code logs/error.log} for warnings and errors</li>
     *     <li>Filters out internal JavaFX logging</li>
     * </ul>
     *
     * @throws IOException if the log file cannot be created or written to
     */
    public static void configureLogger() throws IOException {
        // Obtain the root logger.
        Logger rootLogger = Logger.getLogger("");

        // Remove all existing handlers.
        for (Handler handler : rootLogger.getHandlers())
            rootLogger.removeHandler(handler);

        // Create and configure a ConsoleHandler for colored console output.
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new CustomFormatter());

        // Filter out logs coming from JavaFX internal loggers.
        consoleHandler.setFilter(record -> {
            String loggerName = record.getLoggerName();
            return loggerName == null || !loggerName.startsWith("javafx");
        });

        // Create a FileHandler for logging warnings and errors.
        // The file is located at "logs/error.log" and new messages are appended.
        FileHandler errorFileHandler = new FileHandler("logs/error.log", true);
        errorFileHandler.setLevel(Level.WARNING);
        errorFileHandler.setFormatter(new CustomFormatter());

        // Add the configured handlers to the root logger.
        rootLogger.addHandler(consoleHandler);
        rootLogger.addHandler(errorFileHandler);

        // Set the global logging level to ALL.
        rootLogger.setLevel(Level.ALL);
    }

    /**
     * Custom formatter for logging output.
     * <p>
     * This formatter applies ANSI color codes based on the log level,
     * formats the log record with the date, level, source, and message,
     * and appends the stack trace for any thrown exception.
     */
    private static class CustomFormatter extends Formatter {

        /**
         * Formats a single log record into a string, with optional ANSI color coding and exception stack trace.
         *
         * @param record the log record to format
         * @return the formatted string
         */
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            String color = getColor(record.getLevel());

            sb.append(color)
                    .append(new Date(record.getMillis()))
                    .append(" ")
                    .append(record.getLevel().getName())
                    .append(" [")
                    .append(record.getSourceClassName() != null ? record.getSourceClassName() : record.getLoggerName())
                    .append("] - ")
                    .append(formatMessage(record))
                    .append(System.lineSeparator());

            // If there is an exception, append its stack trace.
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    record.getThrown().printStackTrace(pw);
                }
                sb.append(sw);
            }
            sb.append(RESET);
            return sb.toString();
        }

        /**
         * Returns the appropriate ANSI color code based on the log level.
         *
         * @param level the log level
         * @return ANSI color code string
         */
        private String getColor(Level level) {
            int levelValue = level.intValue();
            if (levelValue >= Level.SEVERE.intValue())
                return RED;
            else if (levelValue >= Level.WARNING.intValue())
                return YELLOW;
            else if (levelValue >= Level.INFO.intValue())
                return GREEN;
            else
                return BLUE;
        }
    }
}
