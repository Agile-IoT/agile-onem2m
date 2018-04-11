package com.srcsolution.things.onem2m_client.osgi;

import java.util.List;

import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import com.srcsolution.things.onem2m_client.ResourceException;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.builder.ApplicationEntityBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;

public class OsgiApplicationEntity extends OsgiResource implements ApplicationEntity {

    protected AE baseAe;

    protected OsgiApplicationEntity(RequestSender requestSender, String resourceId) {
        super(requestSender, resourceId);
        baseAe = (AE) this.om2mResource;
    }

    protected OsgiApplicationEntity(RequestSender requestSender, Resource om2mResource) {
        super(requestSender, om2mResource);
        baseAe = (AE) this.om2mResource;
    }

    @Override
    public void touch() {
        ResponsePrimitive response = requestSender.updateRessource(id, new AE());
        checkStatus(response, ResponseStatusCode.UPDATED);
    }

    @Override
    public String getAppName() {
        return baseAe.getAppName();
    }

    @Override
    public String getAppId() {
        return baseAe.getAppID();
    }

    @Override
    public String getAeId() {
        return baseAe.getAEID();
    }

    @Override
    public List<String> getPointOfAccess() {
        return baseAe.getPointOfAccess();
    }

    @Override
    public String getNodeLinkId() {
        return baseAe.getNodeLink();
    }

    @Override
    public ContainerBuilder buildContainer() {
        return new OsgiContainer.Builder(this);
    }

    public static class Builder extends ApplicationEntityBuilder {

        private final OsgiResource parent;

        public Builder(OsgiResource parent) {
            this.parent = parent;
        }

        @Override
        public ApplicationEntity create(String name) {
            AE ae = new AE();
            ae.setAppID(this.appId != null ? this.appId : name);
            ae.setAppName(name);
            ae.setName(name);
            ae.setRequestReachability(true);
            if (this.labels != null) {
                ae.getLabels().addAll(this.labels);
            }
            if (this.pointOfAccess != null) {
                ae.getPointOfAccess().addAll(this.pointOfAccess);
            }

            ResponsePrimitive resp = parent.requestSender.createAE(ae, name);
            resp = checkStatus(name, resp);

            ae = (AE) resp.getContent();
            return new OsgiApplicationEntity(parent.requestSender, ae.getResourceID());
        }

        private ResponsePrimitive checkStatus(String name, ResponsePrimitive resp) {
            if (!ResponseStatusCode.CREATED.equals(resp.getResponseStatusCode())) {
                if (ResponseStatusCode.ALREADY_EXISTS.equals(resp.getResponseStatusCode())) {
                    resp = parent.requestSender.retrieve(name);
                } else if (ResponseStatusCode.CONFLICT.equals(resp.getResponseStatusCode())) {
                    resp = parent.requestSender.retrieve(name);
                } else {
                    throw new ResourceException(resp.getResponseStatusCode().intValue());
                }
            }
            return resp;
        }

    }

}
