package com.srcsolution.things.onem2m_client.resource;

import java.util.List;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;

public interface Subscription extends Resource {

    default ResourceType getType() {
        return ResourceType.SUBSCRIPTION;
    }

    List<String> getNotificationURI();

    NotificationContentType getNotificationContentType();
    
}
