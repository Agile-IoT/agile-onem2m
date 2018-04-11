package com.srcsolution.things.onem2m_client;

import com.srcsolution.things.onem2m_client.resource.CseBase;

public abstract class ResourceFactory {

    public abstract CseBase retrieveRoot();

    public abstract Resource retrieve(String resourceId);

}
