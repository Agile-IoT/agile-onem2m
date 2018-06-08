package org.eclipse.agail.protocol.om2m.notification;

import com.google.common.base.MoreObjects;

import com.srcsolution.things.onem2m_client.Resource;

public class NotificationResourceStatus extends Notification {

    private ResourceAttributes resource;

    private ResourceStatus status;

    private String subscriptionReference;

    protected NotificationResourceStatus() {
        super();
    }

    public Resource getResource() {
        return resource;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    protected NotificationResourceStatus setResource(ResourceAttributes resource) {
        this.resource = resource;
        return this;
    }

    protected NotificationResourceStatus setStatus(ResourceStatus status) {
        this.status = status;
        return this;
    }

    public String getSubscriptionReference() {
        return subscriptionReference;
    }

    public NotificationResourceStatus setSubscriptionReference(String subscriptionReference) {
        this.subscriptionReference = subscriptionReference;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("resource", resource)
                          .add("status", status)
                          .toString();
    }
}
