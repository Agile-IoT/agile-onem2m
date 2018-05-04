package com.srcsolution.things.onem2m_client.resource;

import java.util.List;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;

public interface CseBase extends Resource {

    default ResourceType getType() {
        return ResourceType.REMOTE_CSE;
    }

    CseType getCseType();

    String getCseId();

    List<String> getPointOfAccess();

    String getNodeLinkId();

}
