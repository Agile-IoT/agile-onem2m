package org.eclipse.agail.protocol.om2m.notification;

import java.io.InputStream;

public abstract class NotificationParser {

    abstract Notification parse(InputStream stream);

    abstract boolean support(String mediaType);

}
