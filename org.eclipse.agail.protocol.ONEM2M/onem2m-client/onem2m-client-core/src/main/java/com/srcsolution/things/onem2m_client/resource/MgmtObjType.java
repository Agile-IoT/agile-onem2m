package com.srcsolution.things.onem2m_client.resource;

public enum MgmtObjType {

    FIRMWARE(1001),
    SOFTWARE(1002),
    MEMORY(1003),
    AREA_NETWORK_INFO(1004),
    AREA_NETWORK_DEVICE_INFO(1005),
    BATTERY(1006),
    DEVICE_INFO(1007),
    DEVICE_CAPABILITY(1008),
    REBOOT(1009),
    UNKNOWN(0);

    int value;

    MgmtObjType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }

    public static MgmtObjType valueOf(int i) {
        for (MgmtObjType type : MgmtObjType.values()) {
            if (i == type.value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MgmtObj type value " + i);
    }

}
