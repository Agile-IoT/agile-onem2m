package com.srcsolution.things.onem2m_client;

public enum ResourceType {

    ACCESS_CONTROL_POLICY(1, "acp"),
    APPLICATION_ENTITY(2, "ae"),
    CONTAINER(3, "cnt"),
    CONTENT_INSTANCE(4, "cin"),
    CSE_BASE(5, "cb"),
    GROUP(9, "grp"),
    MANAGEMENT_OBJECT(13, "mgo"),
    NODE(14, "nod"),
    REMOTE_CSE(16, "csr"),
    SUBSCRIPTION(23, "sub");

    public final int value;

    public final String shortName;

    ResourceType(int value, String shortName) {
        this.value = value;
        this.shortName = shortName;
    }

    public int getValue() {
        return value;
    }

    public String getShortName() {
        return shortName;
    }

    public static ResourceType valueFor(int v) {
        for (ResourceType type : ResourceType.values()) {
            if (type.value == v) {
                return type;
            }
        }
        throw new IllegalArgumentException("Resource type value unknown: " + v);
    }

    public static ResourceType valueFor(String shortName) {
        for (ResourceType type : ResourceType.values()) {
            if (type.shortName.equalsIgnoreCase(shortName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Resource type short name unknown: " + shortName);
    }

}
