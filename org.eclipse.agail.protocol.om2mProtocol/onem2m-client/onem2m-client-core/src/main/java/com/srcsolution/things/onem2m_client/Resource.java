package com.srcsolution.things.onem2m_client;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.srcsolution.things.onem2m_client.resource.NotificationContentType;

public interface Resource {

    // ATTRIBUTES

    String getId();

    String getName();

    String getParentId();

    ResourceType getType();

    List<String> getLabels();

    ZonedDateTime getCreationTime();

    ZonedDateTime getLastModifiedTime();

    String getAttribute(String name);

    String getHierarchicalUrl();

    // CHILDREN RETRIEVE

    List<Resource> children(ResourceType... types);

    Optional<Resource> child(String name);

    Optional<Resource> deepChild(String name, String... names);

    //    Resource parent();

    // DISCOVER

    List<Resource> find(ResourceType type, String... labels);

    Set<Resource> deepFind(ResourceType type, String... labels);

    // WRITE OPERATIONS

    <R extends ResourceBuilder> R buildChild(Class<? extends R> builder);

    Resource updateAttribute(String name, Serializable value);

    //Resource updateAttributes(Map<String, Serializable> values);

    void delete();

    void touch();

    //

    default void subscribe(String subscriptionName, String callbackUrl) {
        subscribe(subscriptionName, callbackUrl, NotificationContentType.WHOLE_RESOURCE);
    }

    void subscribe(String subscriptionName, String callbackUrl, NotificationContentType notificationContentType);

    //

    default boolean hasLabel(String label) {
        for (String l : getLabels()) {
            if (l.equals(label)) {
                return true;
            }
        }
        return false;
    }

    default boolean hasLabelStartWith(String label) {
        for (String l : getLabels()) {
            if (l.startsWith(label)) {
                return true;
            }
        }
        return false;
    }

}
