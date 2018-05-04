package com.srcsolution.things.onem2m_client.resource.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.resource.CseBase;
import com.srcsolution.things.onem2m_client.resource.CseType;

public abstract class CseBaseBuilder extends ResourceBuilder<CseBase> {

    protected CseType cseType;

    protected String cseId;

    protected List<String> pointOfAccess;

    public CseBaseBuilder setCseType(CseType cseType) {
        this.cseType = cseType;
        return this;
    }

    public CseBaseBuilder setCseId(String cseId) {
        this.cseId = cseId;
        return this;
    }

    public CseBaseBuilder setPointOfAccess(List<String> pointOfAccess) {
        this.pointOfAccess = pointOfAccess;
        return this;
    }

    public CseBaseBuilder addPointOfAccess(String... poa) {
        if (poa != null) {
            if (this.pointOfAccess == null) {
                this.pointOfAccess = new ArrayList<>();
            }
            Collections.addAll(this.pointOfAccess, poa);
        }
        return this;
    }

    @Override
    public CseBaseBuilder addLabels(String... labels) {
        return (CseBaseBuilder) super.addLabels(labels);
    }

    @Override
    public CseBaseBuilder setLabels(List<String> labels) {
        return (CseBaseBuilder) super.setLabels(labels);
    }

}
