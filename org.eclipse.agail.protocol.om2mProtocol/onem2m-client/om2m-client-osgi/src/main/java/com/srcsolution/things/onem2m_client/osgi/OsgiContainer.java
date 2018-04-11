package com.srcsolution.things.onem2m_client.osgi;

import java.math.BigInteger;

import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import com.srcsolution.things.onem2m_client.resource.Container;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContentInstanceBuilder;

public class OsgiContainer extends OsgiResource implements Container {

    protected org.eclipse.om2m.commons.resource.Container baseContainer;

    protected OsgiContainer(RequestSender requestSender, String resourceId) {
        super(requestSender, resourceId);
        baseContainer = (org.eclipse.om2m.commons.resource.Container) om2mResource;
    }

    protected OsgiContainer(RequestSender requestSender, Resource om2mResource) {
        super(requestSender, om2mResource);
        baseContainer = (org.eclipse.om2m.commons.resource.Container) om2mResource;
    }

    @Override
    public ContentInstance getLatestInstance() {
        return new OsgiContentInstance(requestSender, baseContainer.getLatest());
    }

    @Override
    public ContentInstance getOldestInstance() {
        return new OsgiContentInstance(requestSender, baseContainer.getOldest());
    }

    @Override
    public long getMaxNumberOfIntance() {
        BigInteger maxNrOfInstances = baseContainer.getMaxNrOfInstances();
        return maxNrOfInstances != null ? maxNrOfInstances.longValue() : -1;
    }

    @Override
    public long getCurrentNumberOfIntance() {
        return baseContainer.getCurrentNrOfInstances().longValue();
    }

    @Override
    public ContainerBuilder buildContainer() {
        return new OsgiContainer.Builder(this);
    }

    @Override
    public ContentInstanceBuilder buildContentInstance() {
        return new OsgiContentInstance.Builder(this);
    }

    public static class Builder extends ContainerBuilder {

        private final OsgiResource parent;

        public Builder(OsgiResource parent) {
            this.parent = parent;
        }

        @Override
        public Container create(String name) {
            org.eclipse.om2m.commons.resource.Container container = new org.eclipse.om2m.commons.resource.Container();
            container.setName(name);
            if (this.labels != null) {
                container.getLabels().addAll(this.labels);
            }
            container.setMaxNrOfInstances(this.maxNumberOfIntance != null ? BigInteger.valueOf(maxNumberOfIntance) : null);

            ResponsePrimitive resp = parent.requestSender.createContainer(parent.getId(), name, container);

            org.eclipse.om2m.commons.resource.Container content = (org.eclipse.om2m.commons.resource.Container) resp.getContent();
            return new OsgiContainer(parent.requestSender, content.getResourceID());
        }

    }

}
