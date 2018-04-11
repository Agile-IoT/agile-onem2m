package com.srcsolution.things.onem2m_client.osgi;

import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import com.srcsolution.things.onem2m_client.resource.ContentInstance;
import com.srcsolution.things.onem2m_client.resource.builder.ContentInstanceBuilder;

public class OsgiContentInstance extends OsgiResource implements ContentInstance {

    protected org.eclipse.om2m.commons.resource.ContentInstance baseContentInstance;

    protected OsgiContentInstance(RequestSender requestSender, String resourceId) {
        super(requestSender, resourceId);
        baseContentInstance = (org.eclipse.om2m.commons.resource.ContentInstance) om2mResource;
    }

    protected OsgiContentInstance(RequestSender requestSender, Resource om2mResource) {
        super(requestSender, om2mResource);
        baseContentInstance = (org.eclipse.om2m.commons.resource.ContentInstance) om2mResource;
    }

    @Override
    public String getContentInfo() {
        return baseContentInstance.getContentInfo();
    }

    @Override
    public String getContent() {
        return baseContentInstance.getContent();
    }

    @Override
    public long getContentSize() {
        return baseContentInstance.getContentSize().longValue();
    }

    public static class Builder extends ContentInstanceBuilder {

        private final OsgiResource parent;

        public Builder(OsgiResource parent) {
            this.parent = parent;
        }

        @Override
        public ContentInstance create(String name) {
            org.eclipse.om2m.commons.resource.ContentInstance instance = new org.eclipse.om2m.commons.resource.ContentInstance();
            instance.setName(name);
            if (this.labels != null) {
                instance.getLabels().addAll(this.labels);
            }
            instance.setContentInfo(this.contentInfo);
            instance.setContent(this.content);

            ResponsePrimitive resp = parent.requestSender.createContentInstance(parent.getId(), name, instance);

            org.eclipse.om2m.commons.resource.ContentInstance content = (org.eclipse.om2m.commons.resource.ContentInstance) resp.getContent();
            return new OsgiContentInstance(parent.requestSender, content.getResourceID());
        }

    }

}
