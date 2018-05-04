package com.srcsolution.things.interworking.data;

import java.util.*;
import java.util.regex.Pattern;

import embedded_libs.com.google.common.base.MoreObjects;

import com.srcsolution.things.onem2m_client.Resource;

public class Thing {

    private String id;

    private String name;

    private HashMap<String, String> additionalProperties;

    private Device device;

    private Connectivity connectivity;

    private List<Topic> topics;

    private Set<OperationData> operations;

    private Resource om2mResource;

    public Thing() {
    }

    public Thing(String id) {
        setId(id);
    }

    public String getId() {
        return id;
    }

    public Thing setId(String id) {
        validateId(id);
        this.id = id;
        return this;
    }

    public static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9_\\-~:]*");

    public static void validateId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must be not null");
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid String");
        }
    }

    public String getName() {
        return name;
    }

    public Thing setName(String name) {
        this.name = name;
        return this;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Connectivity getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(Connectivity connectivity) {
        this.connectivity = connectivity;
    }

    public List<Topic> getTopics() {
        if (this.topics == null) {
            this.topics = new ArrayList<>();
        }
        return this.topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public Set<OperationData> getOperations() {
        if (operations == null) {
            operations = new HashSet<>();
        }
        return operations;
    }

    public Thing setOperations(Set<OperationData> operations) {
        this.operations = operations;
        return this;
    }

    public Thing addOperation(OperationData operation) {
        getOperations().add(operation);
        return this;
    }

    public HashMap<String, String> getAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        return additionalProperties;
    }

    public void setAdditionalProperties(HashMap<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void addProperty(String name, String value) {
        getAdditionalProperties().put(name, value);
    }

    public Resource getOm2mResource() {
        return om2mResource;
    }

    public void setOm2mResource(Resource om2mResource) {
        this.om2mResource = om2mResource;
    }

    public boolean hasPropertiesChanged(Thing other) {
        if (other == null) return true;

        int currentHash = Objects.hash(this.id, this.name, this.additionalProperties);
        int newHash = Objects.hash(other.id, other.name, other.additionalProperties);

        return currentHash != newHash;
    }

    public boolean hasOperationsChanged(Thing other) {
        return other == null || !Objects.deepEquals(other.getOperations(), other.getOperations());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("name", name)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Thing thing = (Thing) o;
        return Objects.equals(getId(), thing.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
