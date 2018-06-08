package org.eclipse.agail.protocol.om2m.notification;


public class IllegalNotificationException extends RuntimeException {

    public IllegalNotificationException(String msg) {
        super(msg);
    }

    public IllegalNotificationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
