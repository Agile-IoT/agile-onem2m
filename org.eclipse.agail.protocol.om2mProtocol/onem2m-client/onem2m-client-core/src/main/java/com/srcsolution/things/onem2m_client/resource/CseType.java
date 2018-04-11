package com.srcsolution.things.onem2m_client.resource;

public enum CseType {

    INFRASTRUCTURE_NODE(1),
    MIDDLE_NODE(2),
    APPLICATION_SERVICE_NODE(3);

    int value;

    CseType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }

    public static CseType valueOf(int i) {
        for (CseType cseType : CseType.values()) {
            if (i == cseType.value) {
                return cseType;
            }
        }
        throw new IllegalArgumentException("Unknown CSE type value " + i);
    }

}
