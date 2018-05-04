package com.srcsolution.things.onem2m_client.resource.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;

public abstract class ApplicationEntityBuilder extends ResourceBuilder<ApplicationEntity> {

    protected String appName;

    protected String appId;

    protected String aeId;

    protected List<String> pointOfAccess;

    public ApplicationEntityBuilder setAeId(String aeId) {
        this.aeId = aeId;
        return this;
    }

    public ApplicationEntityBuilder setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public ApplicationEntityBuilder setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public ApplicationEntityBuilder addPointOfAccess(String... poa) {
        if (poa != null) {
            if (this.pointOfAccess == null) {
                this.pointOfAccess = new ArrayList<>();
            }
            Collections.addAll(this.pointOfAccess, poa);
        }
        return this;
    }

    @Override
    public ApplicationEntityBuilder addLabels(String... labels) {
        return (ApplicationEntityBuilder) super.addLabels(labels);
    }

    @Override
    public ApplicationEntityBuilder setLabels(List<String> labels) {
        return (ApplicationEntityBuilder) super.setLabels(labels);
    }

}
