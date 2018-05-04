package org.eclipse.agail.protocol.om2m.properties;

import org.eclipse.agail.protocol.om2m.Om2mProperties;
import org.slf4j.MDC;

public interface LoggerConfigurator {

    void configure(Om2mProperties props, String loggerName);

    static void putMDC(String ipeName) {
        MDC.put("PROTOCOL", ipeName);
    }

}
