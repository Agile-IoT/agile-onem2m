package org.eclipse.agail.protocol.om2m.properties;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

import org.eclipse.agail.protocol.om2m.Om2mProperties;

public class JulConfigurator implements LoggerConfigurator {

    @Override
    public void configure(Om2mProperties props, String loggerName) {
        try {
            // Load a properties file from class path that way can't be achieved with java.util.logging.config.file
            /*
            final LogManager logManager = LogManager.getLogManager();
            try (final InputStream is = getClass().getResourceAsStream("/logging.properties")) {
                logManager.readConfiguration(is);
            }
            */

            // Programmatic configuration
            //            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %4$s: %2$s - %5$s %6$s%n");

            MyFormatter formatter = new MyFormatter();

            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.FINE);
            consoleHandler.setFormatter(formatter);

            FileHandler fileHandler = new FileHandler("/tmp/myLog/" + loggerName + ".%g.log", 10_000_000, 5, true);
            fileHandler.setFormatter(formatter);

            final Logger app = Logger.getLogger("com.srcsolution.things");
            app.setLevel(Level.FINEST);
            app.addHandler(consoleHandler);
            app.addHandler(fileHandler);

        } catch (Exception e) {
            // The runtime won't show stack traces if the exception is thrown
            e.printStackTrace();
        }
    }

    static class MyFormatter extends Formatter {

        private static final String format = "[%1$tc] %4$s: %2$s - %5$s %6$s%n";

        private final Date dat = new Date();

        @Override
        public synchronized String format(LogRecord record) {
            dat.setTime(record.getMillis());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(format,
                                 dat,
                                 source,
                                 record.getLoggerName(),
                                 record.getLevel(),
                                 Thread.currentThread().getName(),
                                 message,
                                 throwable);
        }
    }

}
