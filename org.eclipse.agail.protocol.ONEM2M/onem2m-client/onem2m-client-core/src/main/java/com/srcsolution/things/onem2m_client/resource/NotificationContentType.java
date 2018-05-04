package com.srcsolution.things.onem2m_client.resource;

public enum NotificationContentType {

    WHOLE_RESOURCE(1),
    MODIFIED_ATTRIBUTES(2),
    REFERENCE_ONLY(3);

    int value;

    NotificationContentType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }

    public static NotificationContentType valueOf(int i) {
        for (NotificationContentType nct : NotificationContentType.values()) {
            if (i == nct.value) {
                return nct;
            }
        }
        throw new IllegalArgumentException("Unknown notification content type value " + i);
    }

}
