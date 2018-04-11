package com.srcsolution.things.onem2m_client.osgi;

import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import com.srcsolution.things.onem2m_client.resource.Node;
import com.srcsolution.things.onem2m_client.resource.builder.NodeBuilder;

public class OsgiNode extends OsgiResource implements Node {

    private final org.eclipse.om2m.commons.resource.Node base;

    public OsgiNode(RequestSender requestSender) {
        this(requestSender, null);
    }

    public OsgiNode(RequestSender requestSender, String resourceId) {
        super(requestSender, resourceId);
        base = (org.eclipse.om2m.commons.resource.Node) this.om2mResource;
    }

    @Override
    public String getNodeId() {
        return base.getNodeID();
    }

    public static class Builder extends NodeBuilder {

        private final OsgiResource parent;

        public Builder(OsgiResource parent) {
            this.parent = parent;
        }

        @Override
        public Node create(String name) {
            org.eclipse.om2m.commons.resource.Node instance = new org.eclipse.om2m.commons.resource.Node();
            instance.setName(name);
            if (this.labels != null) {
                instance.getLabels().addAll(this.labels);
            }
            instance.setNodeID(this.nodeId);

            ResponsePrimitive resp = parent.requestSender.createResource(parent.getId(), name, instance, ResourceType.NODE);

            org.eclipse.om2m.commons.resource.Node content = (org.eclipse.om2m.commons.resource.Node) resp.getContent();
            return new OsgiNode(parent.requestSender, content.getResourceID());
        }

    }

}
