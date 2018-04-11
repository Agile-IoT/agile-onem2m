package com.srcsolution.things.onem2m_client.resource.builder;

import java.util.List;

import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;

public abstract class ContentInstanceBuilder extends ResourceBuilder<ContentInstance> {

    protected String content;

    protected String contentInfo;

    public ContentInstanceBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    public ContentInstanceBuilder setContentInfo(String contentInfo) {
        this.contentInfo = contentInfo;
        return this;
    }

    @Override
    public ContentInstanceBuilder addLabels(String... labels) {
        return (ContentInstanceBuilder) super.addLabels(labels);
    }

    @Override
    public ContentInstanceBuilder setLabels(List<String> labels) {
        return (ContentInstanceBuilder) super.setLabels(labels);
    }

}
