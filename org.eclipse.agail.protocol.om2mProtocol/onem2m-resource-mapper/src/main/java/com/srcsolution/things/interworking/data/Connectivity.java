package com.srcsolution.things.interworking.data;

import java.util.HashMap;
import java.util.Objects;

import com.srcsolution.things.onem2m_client.resource.Container;

public class Connectivity {

    private String id;

    private ConnectivityStatus status;

    private String rawStatus;

    private String networkType;

    private Container om2mResource;

    private HashMap<String, Object> additionalProperties;

    public Connectivity() {
    }

    public Connectivity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Connectivity setId(String id) {
        this.id = id;
        return this;
    }

    public ConnectivityStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectivityStatus status) {
        this.status = status;
    }

    public String getRawStatus() {
        return rawStatus;
    }

    public void setRawStatus(String rawStatus) {
        this.rawStatus = rawStatus;
    }

    public String getNetworkType() {
        return networkType;
    }

    public Connectivity setNetworkType(String networkType) {
        this.networkType = networkType;
        return this;
    }

    public HashMap<String, Object> getAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        return additionalProperties;
    }

    public void setAdditionalProperties(HashMap<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void addProperty(String name, String value) {
        getAdditionalProperties().put(name, value);
    }

    public Container getOm2mResource() {
        return om2mResource;
    }

    public void setOm2mResource(Container om2mResource) {
        this.om2mResource = om2mResource;
    }

    public boolean hasChanged(Connectivity other) {
        if (other == null) return true;

        int currentHash = Objects.hash(this.id, this.additionalProperties, this.status, this.rawStatus, this.networkType);
        int newHash = Objects.hash(other.id, other.additionalProperties, other.status, other.rawStatus, other.networkType);

        return currentHash != newHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connectivity that = (Connectivity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
