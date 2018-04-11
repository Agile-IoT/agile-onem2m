package com.srcsolution.things.onem2m_client.resource;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;

public abstract class AbstractResource implements Resource {

    protected String id;

    protected String parentId;

    protected String name;

    protected ResourceType type;

    protected ZonedDateTime creationTime;

    protected ZonedDateTime lastModifiedTime;

    protected List<String> labels;

    protected Map<String, String> attributes;

    protected String hierarchicalUrl;

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public List<String> getLabels() {
        return labels;
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneId.systemDefault());

    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    public ZonedDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    protected AbstractResource setCreationTime(ZonedDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    protected AbstractResource setId(String id) {
        this.id = id;
        return this;
    }

    protected AbstractResource setLabels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    protected AbstractResource setLastModifiedTime(ZonedDateTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }

    protected AbstractResource setName(String name) {
        this.name = name;
        return this;
    }

    protected AbstractResource setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    protected AbstractResource setType(ResourceType type) {
        this.type = type;
        return this;
    }

    protected AbstractResource setType(int type) {
        this.type = ResourceType.valueFor(type);
        return this;
    }

    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    protected AbstractResource setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public String getHierarchicalUrl() {
        return hierarchicalUrl;
    }

    protected AbstractResource setHierarchicalUrl(String hierarchicalUrl) {
        this.hierarchicalUrl = hierarchicalUrl;
        return this;
    }

    @Override
    public Optional<Resource> child(String name) {
        return children().stream()
                         .filter(resource -> Objects.equals(resource.getName(), name))
                         .findFirst();
    }

    @Override
    public String toString() {
        return "Resource[" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractResource that = (AbstractResource) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getType());
    }

}
