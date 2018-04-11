package org.eclipse.agail.protocol.om2m.notification;


public class InvalidNotificationException extends RuntimeException {

    public InvalidNotificationException(String msg) {
        super(msg);
    }

    public InvalidNotificationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
