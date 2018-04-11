package org.eclipse.agail.protocol.om2m.notification;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.AbstractResource;
import com.srcsolution.things.onem2m_client.resource.NotificationContentType;

public class ResourceAttributes extends AbstractResource {

    public AbstractResource setAttribute(String name, String value) {
        getAttributes().put(name, value);
        return this;
    }

    @Override
    public AbstractResource setCreationTime(ZonedDateTime creationTime) {
        return super.setCreationTime(creationTime);
    }

    @Override
    public AbstractResource setId(String id) {
        return super.setId(id);
    }

    @Override
    public AbstractResource setLabels(List<String> labels) {
        return super.setLabels(labels);
    }

    @Override
    public AbstractResource setLastModifiedTime(ZonedDateTime lastModifiedTime) {
        return super.setLastModifiedTime(lastModifiedTime);
    }

    @Override
    public AbstractResource setName(String name) {
        return super.setName(name);
    }

    @Override
    public AbstractResource setParentId(String parentId) {
        return super.setParentId(parentId);
    }

    @Override
    public AbstractResource setType(ResourceType type) {
        return super.setType(type);
    }

    @Override
    public AbstractResource setType(int type) {
        return super.setType(type);
    }

    @Override
    public Map<String, String> getAttributes() {
        return super.getAttributes();
    }

    @Override
    public AbstractResource setAttributes(Map<String, String> attributes) {
        return super.setAttributes(attributes);
    }

    @Override
    public String getAttribute(String name) {
        return getAttributes().get(name);
    }

    @Override
    public List<Resource> children(ResourceType... types) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Resource> deepChild(String name, String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> find(ResourceType type, String... labels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Resource> deepFind(ResourceType type, String... labels) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends ResourceBuilder> R buildChild(Class<? extends R> builder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource updateAttribute(String name, Serializable value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subscribe(String subscriptionName, String callbackUrl, NotificationContentType notificationContentType) {
        throw new UnsupportedOperationException();
    }

}
