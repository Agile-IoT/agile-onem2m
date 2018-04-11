package com.srcsolution.things.onem2m_client.resource.builder;

import java.util.List;

import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.resource.Container;

public abstract class ContainerBuilder extends ResourceBuilder<Container> {

    protected Long maxNumberOfIntance;

    public ContainerBuilder setMaxNumberOfIntance(Long maxNumberOfIntance) {
        this.maxNumberOfIntance = maxNumberOfIntance;
        return this;
    }

    @Override
    public ContainerBuilder addLabels(String... labels) {
        return (ContainerBuilder) super.addLabels(labels);
    }

    @Override
    public ContainerBuilder setLabels(List<String> labels) {
        return (ContainerBuilder) super.setLabels(labels);
    }

}
