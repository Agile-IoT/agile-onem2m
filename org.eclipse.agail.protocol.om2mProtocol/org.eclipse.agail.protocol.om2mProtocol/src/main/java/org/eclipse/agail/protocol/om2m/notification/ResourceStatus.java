package org.eclipse.agail.protocol.om2m.notification;

public enum ResourceStatus {

    CHILD_CREATED(1),
    CHILD_DELETED(2),
    UPDATED(3),
    DELETED(4);

    private final int value;

    ResourceStatus(int i) {

        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public static ResourceStatus valueOf(int i) {
        for (ResourceStatus nct : ResourceStatus.values()) {
            if (i == nct.value) {
                return nct;
            }
        }
        throw new IllegalArgumentException("Unknown resource status value " + i);
    }

}
