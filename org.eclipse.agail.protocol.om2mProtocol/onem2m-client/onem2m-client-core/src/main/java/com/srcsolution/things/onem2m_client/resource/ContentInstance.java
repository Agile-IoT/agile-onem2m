package com.srcsolution.things.onem2m_client.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.ResourceException;
import com.srcsolution.things.onem2m_client.ResourceType;

public interface ContentInstance extends Resource {

    default ResourceType getType() {
        return ResourceType.CONTENT_INSTANCE;
    }

    String getContentInfo();

    String getContent();

    long getContentSize();

    @Override
    default <R extends ResourceBuilder> R buildChild(Class<? extends R> resourceType) {
        throw new ResourceException("ContentInstance cannot have a child resource", 4005);
    }

    @Override
    default Resource updateAttribute(String name, Serializable value) {
        throw new ResourceException("Update of ContentInstance is not allowed", 4005);
    }

    @Override
    default Optional<Resource> child(String name) {
        return Optional.empty();
    }

    @Override
    default List<Resource> children(ResourceType... types) {
        return Collections.emptyList();
    }

    @Override
    default List<Resource> find(ResourceType type, String... labels) {
        return Collections.emptyList();
    }

    @Override
    default Set<Resource> deepFind(ResourceType type, String... labels) {
        return Collections.emptySet();
    }

    @Override
    default void subscribe(String subscriptionName, String callbackUrl, NotificationContentType notificationContentType) {
        throw new ResourceException("Subscribe to ContentInstance is not allowed", 4005);
    }

}
