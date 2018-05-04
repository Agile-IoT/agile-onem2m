package com.srcsolution.things.onem2m_client.http;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.fluent.Request;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ShortName;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.ResourceFactory;

public class HttpResourceFactory extends ResourceFactory {

    private final Node node;

    protected OneM2mHttpClient httpClient;

    public HttpResourceFactory(Node node) {
        this.node = node;
        this.httpClient = new OneM2mHttpClient(node);
    }

    @Override
    public HttpCseBase retrieveRoot() {
        return new HttpCseBase(node, node.getId());
    }

    @Override
    public HttpResource retrieve(String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) {
            return retrieveRoot();
        } else {
            return create(node, resourceId);
        }
    }

    private JsonNode retrieveJson(String resourceId) {
        Request request = httpClient.buildGetRequest(resourceId);
        return httpClient.executeRequestAndParse(request);
    }

    private HttpResource create(Node node, String resourceId) {
        JsonNode jsonNode = retrieveJson(resourceId);
        int type = jsonNode.path(ShortName.TYPE).asInt();
        switch (type) {
            case ResourceType.CSE_BASE:
                return new HttpCseBase(node, jsonNode);
            case ResourceType.AE:
                return new HttpApplicationEntity(node, jsonNode);
            case ResourceType.CONTAINER:
                return new HttpContainer(node, jsonNode);
            case ResourceType.CONTENT_INSTANCE:
                return new HttpContentInstance(node, jsonNode);
        }
        return new HttpResource(node, resourceId);
    }

}
