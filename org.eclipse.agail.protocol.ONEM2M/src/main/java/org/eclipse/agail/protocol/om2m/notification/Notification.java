package org.eclipse.agail.protocol.om2m.notification;

import java.time.LocalDateTime;

public abstract class Notification {

    private final LocalDateTime receivedDate;

    public Notification() {
        receivedDate = LocalDateTime.now();
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

}
