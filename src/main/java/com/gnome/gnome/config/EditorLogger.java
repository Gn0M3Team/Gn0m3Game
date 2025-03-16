package com.gnome.gnome.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

/**
 *  This log is created for the editor page, for testing the function,
 *  but if you need a logger, you can use it, but don't change anything.
 *  Write to me and I'll change it myself, or create your own.
 *  Thank you for your understanding
 */
public class EditorLogger {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    public static void configureLogger() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers())
            rootLogger.removeHandler(handler);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new CustomFormatter());

        consoleHandler.setFilter(record -> {
            String loggerName = record.getLoggerName();
            return loggerName == null || !loggerName.startsWith("javafx");
        });

        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.ALL);
    }

    private static class CustomFormatter extends Formatter {
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
