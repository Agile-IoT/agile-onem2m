package com.srcsolution.things.onem2m_client.osgi;

import java.util.List;

import org.eclipse.om2m.commons.resource.CSEBase;
import org.eclipse.om2m.commons.resource.Resource;

import com.srcsolution.things.onem2m_client.resource.CseBase;
import com.srcsolution.things.onem2m_client.resource.CseType;

public class OsgiCseBase extends OsgiResource implements CseBase {

    private final CSEBase base;

    protected OsgiCseBase(RequestSender requestSender, String resourceId) {
        super(requestSender, resourceId);
        base = (CSEBase) this.om2mResource;
    }

    public OsgiCseBase(RequestSender requestSender, Resource om2mResource) {
        super(requestSender, om2mResource);
        base = (CSEBase) this.om2mResource;
    }

    @Override
    public CseType getCseType() {
        return CseType.valueOf(base.getCseType().intValue());
    }

    @Override
    public String getCseId() {
        return base.getCSEID();
    }

    @Override
    public List<String> getPointOfAccess() {
        return base.getPointOfAccess();
    }

    @Override
    public String getNodeLinkId() {
        return base.getNodeLink();
    }

}
