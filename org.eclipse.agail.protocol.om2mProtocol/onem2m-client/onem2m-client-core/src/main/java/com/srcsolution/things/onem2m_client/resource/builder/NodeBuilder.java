package com.srcsolution.things.onem2m_client.resource.builder;

import java.util.List;

import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.resource.Node;

public abstract class NodeBuilder extends ResourceBuilder<Node> {

    protected String nodeId;

    public NodeBuilder setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    @Override
    public NodeBuilder addLabels(String... labels) {
        return (NodeBuilder) super.addLabels(labels);
    }

    @Override
    public NodeBuilder setLabels(List<String> labels) {
        return (NodeBuilder) super.setLabels(labels);
    }

}
