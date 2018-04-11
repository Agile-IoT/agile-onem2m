package com.srcsolution.things.onem2m_client.osgi;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.core.service.CseService;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.ResourceFactory;

public class OsgiResourceFactory extends ResourceFactory {

    private final Node node;

    private final CseService cseService;

    private final RequestSender requestSender;

    public OsgiResourceFactory(Node node, CseService cseService) {
        this.node = node;
        this.cseService = cseService;
        this.requestSender = new RequestSender(cseService);
    }

    @Override
    public OsgiCseBase retrieveRoot() {
        return new OsgiCseBase(requestSender, "/" + Constants.CSE_ID);
    }

    @Override
    public OsgiResource retrieve(String resourceId) {
        return new OsgiResource(requestSender, resourceId);
    }

}
