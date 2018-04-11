package com.srcsolution.things.interworking.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.srcsolution.things.onem2m_client.resource.Container;

public class Topic {

    private String name;

    private List<Message> messages;

    private long maxInstances;

    private Container om2mResource;

    public Topic() {
        this.maxInstances = 20L;
    }

    public Topic(String name) {
        this();
        this.name = name;
    }

    public Topic(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public Topic(long maxInstances) {
        this.maxInstances = maxInstances;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        if (messages != null) {
            messages.forEach(message -> message.setTopic(this));
        }
    }

    public long getMaxInstances() {
        return maxInstances;
    }

    public void setMaxInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public Container getOm2mResource() {
        return om2mResource;
    }

    public void setOm2mResource(Container om2mResource) {
        this.om2mResource = om2mResource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(getName(), topic.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
