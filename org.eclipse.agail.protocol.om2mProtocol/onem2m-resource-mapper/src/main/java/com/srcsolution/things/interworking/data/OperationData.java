package com.srcsolution.things.interworking.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OperationData {

    protected String name;

    private String url;

    private Map<String, Class> parameters;

    public OperationData() {
    }

    public OperationData(String name) {
        this.name = name;
    }

    public OperationData(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public OperationData setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, Class> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters;
    }

    public OperationData setParameters(Map<String, Class> parameters) {
        this.parameters = parameters;
        return this;
    }

    public OperationData addParameter(String name, Class type) {
        getParameters().put(name, type);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public OperationData setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationData operation = (OperationData) o;
        return Objects.equals(getName(), operation.getName()) &&
                Objects.equals(getUrl(), operation.getUrl()) &&
                Objects.equals(getParameters(), operation.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUrl(), getParameters());
    }
}
