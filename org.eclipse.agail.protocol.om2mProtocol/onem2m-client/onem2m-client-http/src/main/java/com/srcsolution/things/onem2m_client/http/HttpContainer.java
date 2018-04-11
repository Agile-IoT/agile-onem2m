package com.srcsolution.things.onem2m_client.http;

import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.JsonNode;
import embedded_libs.com.google.common.base.Joiner;
import embedded_libs.com.google.common.base.Strings;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ShortName;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.resource.Container;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContentInstanceBuilder;

public class HttpContainer extends HttpResource implements Container {

    protected HttpContainer(Node node, String resourceId) {
        super(node, resourceId);
    }

    protected HttpContainer(Node node, JsonNode json) {
        super(node, json);
    }

    @Override
    public ContentInstance getLatestInstance() {
        //        if (getCurrentNumberOfIntance() > 0) {
        return new HttpContentInstance(node, cache.path(ShortName.LATEST).asText());
        //        } else {
        //            return new ResourceException("No child resource found", StatusCode.STATUS_NOT_FOUND);
        //        }
    }

    @Override
    public ContentInstance getOldestInstance() {
        return new HttpContentInstance(node, cache.path(ShortName.OLDEST).asText());
    }

    @Override
    public long getMaxNumberOfIntance() {
        return cache.path(ShortName.MAX_NR_OF_INSTANCES).asLong();
    }

    @Override
    public long getCurrentNumberOfIntance() {
        return cache.path(ShortName.CURRENT_NUMBER_OF_INSTANCES).asLong();
    }

    @Override
    public ContainerBuilder buildContainer() {
        return new HttpContainer.Builder(this);
    }

    @Override
    public ContentInstanceBuilder buildContentInstance() {
        return new HttpContentInstance.Builder(this);
    }

    @Override
    protected String wrapBody(String body) {
        return wrapXmlBody(body);
    }

    public static String wrapXmlBody(String body) {
        return "<om2m:cnt xmlns:om2m=\"http://www.onem2m.org/xml/protocols\">"
                + body
                + "</om2m:cnt>";
    }

    public static class Builder extends ContainerBuilder {

        private final HttpResource parent;

        public Builder(HttpResource parent) {
            this.parent = parent;
        }

        @Override
        public Container create(String name) {
            String body = wrapXmlBody(
                    (this.maxNumberOfIntance != null ? "<mni>" + this.maxNumberOfIntance + "</mni>" : "") +
                            (this.labels != null ? "<lbl>" + Joiner.on(" ").join(labels) + "</lbl>" : ""));

            try {
                URI uri = parent.httpClient.buildUri(parent.getId()).build();
                Request request = Request.Post(uri)
                                         .bodyString(body, ContentType.parse("application/xml;ty=" + ResourceType.CONTAINER));
                if (!Strings.isNullOrEmpty(name)) {
                    request = request.addHeader("X-M2M-NM", name);
                }
                JsonNode jsonNode = parent.httpClient.executeRequestAndParse(request);
                return new HttpContainer(parent.node, jsonNode);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot build URI", e);
            }
        }

    }

}
