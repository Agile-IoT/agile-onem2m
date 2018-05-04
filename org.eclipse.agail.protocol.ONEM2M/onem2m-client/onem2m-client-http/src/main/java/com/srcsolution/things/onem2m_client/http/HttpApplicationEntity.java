package com.srcsolution.things.onem2m_client.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import embedded_libs.com.google.common.base.Joiner;
import embedded_libs.com.google.common.base.MoreObjects;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ShortName;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.builder.ApplicationEntityBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;

public class HttpApplicationEntity extends HttpResource implements ApplicationEntity {

    protected HttpApplicationEntity(Node node, String resourceId) {
        super(node, resourceId);
    }

    protected HttpApplicationEntity(Node node, JsonNode json) {
        super(node, json);
    }

    @Override
    public String getAppName() {
        return cache.path(ShortName.APP_NAME).asText();
    }

    @Override
    public String getAppId() {
        return cache.path(ShortName.APP_ID).asText();
    }

    @Override
    public String getAeId() {
        return cache.path(ShortName.AE_ID).asText();
    }

    @Override
    public List<String> getPointOfAccess() {
        return parseStringArray(cache.path(ShortName.POA));
    }

    @Override
    public String getNodeLinkId() {
        return cache.path(ShortName.NODE_LINK).asText();
    }

    @Override
    public void touch() {
        try {
            URI uri = httpClient.buildUri(getId()).build();
            Request request = Request.Put(uri)
                                     .addHeader("X-M2M-NM", name)
                                     .bodyString(wrapBody(""), ContentType.parse("application/xml;ty=" + ResourceType.AE));
            httpClient.executeRequestAndParse(request);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot build URI", e);
        }
    }

    @Override
    protected String wrapBody(String body) {
        return wrapXmlBody(body);
    }

    private static String wrapXmlBody(String body) {
        return "<om2m:ae xmlns:om2m=\"http://www.onem2m.org/xml/protocols\">"
                + body
                + "</om2m:ae>";
    }

    @Override
    public ContainerBuilder buildContainer() {
        return new HttpContainer.Builder(this);
    }

    public static class Builder extends ApplicationEntityBuilder {

        private final HttpResource parent;

        public Builder(HttpResource parent) {
            this.parent = parent;
        }

        @Override
        public ApplicationEntity create(String name) {
            String body = wrapXmlBody(
                    "<api>" + MoreObjects.firstNonNull(this.appId, name) + "</api>" +
                            "<rr>true</rr>" +
                            (this.appName != null ? "<apn>" + appName + "</apn>" : "") +
                            (this.aeId != null ? "<aei>" + aeId + "</aei>" : "") +
                            (this.labels != null ? "<lbl>" + Joiner.on(" ").join(labels) + "</lbl>" : "") +
                            (this.pointOfAccess != null ? "<poa>" + Joiner.on(" ").join(pointOfAccess) + "</poa>" : ""));

            try {
                URI uri = parent.httpClient.buildUri(parent.getId()).build();
                Request request = Request.Post(uri)
                                         .addHeader("X-M2M-NM", name)
                                         .bodyString(body, ContentType.parse("application/xml;ty=" + ResourceType.AE));
                JsonNode jsonNode = parent.httpClient.executeRequestAndParse(request);
                return new HttpApplicationEntity(parent.node, jsonNode);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot build URI", e);
            }
        }

    }

}
