package org.eclipse.agail.protocol.om2m.properties;

import java.nio.file.Path;
import java.nio.file.Paths;

import embedded_libs.com.google.common.base.Strings;
import org.apache.log4j.*;
import org.eclipse.agail.protocol.om2m.Om2mProperties;

public class Log4jConfigurator implements LoggerConfigurator {

    @Override
    public void configure(Om2mProperties props, String loggerName) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.getLoggerRepository().resetConfiguration();

        rootLogger.setLevel(Level.INFO);

        String propName = Strings.defaultIfEmpty(props.getName(), loggerName);
        LoggerConfigurator.putMDC(propName);

        // create pattern layout
        PatternLayout layout = new PatternLayout();
//        String conversionPattern = "%d %5p %16.16t (%34.34c{3}) - %m%n";
        //1980-01-11 08:03:59,390 [pool-3-thread-1] DEBUG - c.s.t.i.i.ioports.at.IoportsAtSender - Send AT command : !FWD?GPIO,2

        String conversionPattern = "%d %5p [%16t] - %36c{3} - %m%n";
        layout.setConversionPattern(conversionPattern);

        // root level
        String propRootLevel = props.getProperty("ipe.log.root.level");
        rootLogger.setLevel(Level.toLevel(propRootLevel, Level.INFO));

        // ipe level
        Logger ipeLogger = Logger.getLogger("com.srcsolution.things");
        String propLevel = props.getProperty("ipe.log.level");
        ipeLogger.setLevel(Level.toLevel(propLevel, Level.DEBUG));

        // console appender
        if (props.getProperty("ipe.log.console", Boolean.class, Boolean.FALSE)) {
            Appender consoleAppender = new ConsoleAppender(layout);
            consoleAppender.setName("ipe-console");

            ipeLogger.addAppender(consoleAppender);
            ipeLogger.setAdditivity(false);

            rootLogger.addAppender(consoleAppender);
        }

        // file appender
        String propFile = props.getProperty("ipe.log.file");
        String propPath = props.getProperty("ipe.log.path");
        Path logFilePath = null;
        if (!Strings.isNullOrEmpty(propPath)) {
            logFilePath = Paths.get(propPath);
        }
        if (!Strings.isNullOrEmpty(propFile)) {
            if (logFilePath == null) {
                logFilePath = Paths.get(System.getProperty("user.dir"));
            }
            logFilePath = logFilePath.resolve(propFile);
        } else if (logFilePath != null) {
            logFilePath = logFilePath.resolve("ipe-" + propName + ".log");
        }

        if (logFilePath != null) {
            RollingFileAppender fileAppender = new RollingFileAppender();
            fileAppender.setName("ipe-file");
            fileAppender.setFile(logFilePath.toString());
            fileAppender.setMaxFileSize("1MB");
            fileAppender.setMaxBackupIndex(3);
            fileAppender.setAppend(true);
            //            fileAppender.addFilter(new Filter() {
            //                @Override
            //                public int decide(LoggingEvent event) {
            //                    return propName.equals(event.getMDC("ipe")) ? ACCEPT : DENY;
            //                }
            //            });

            fileAppender.setLayout(layout);
            fileAppender.activateOptions();

            ipeLogger.addAppender(fileAppender);
            ipeLogger.setAdditivity(false);

            rootLogger.addAppender(fileAppender);
        }
    }

}
