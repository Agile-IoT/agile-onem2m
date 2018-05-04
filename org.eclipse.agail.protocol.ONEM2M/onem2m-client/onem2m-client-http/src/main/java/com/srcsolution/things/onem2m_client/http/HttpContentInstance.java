package com.srcsolution.things.onem2m_client.http;

import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.JsonNode;
import embedded_libs.com.google.common.base.Joiner;
import embedded_libs.com.google.common.base.Strings;
import embedded_libs.com.google.common.xml.XmlEscapers;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ShortName;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;
import com.srcsolution.things.onem2m_client.resource.builder.ContentInstanceBuilder;

public class HttpContentInstance extends HttpResource implements ContentInstance {

    protected HttpContentInstance(Node node, String resourceId) {
        super(node, resourceId);
    }

    protected HttpContentInstance(Node node, JsonNode json) {
        super(node, json);
    }

    @Override
    public String getContentInfo() {
        return cache.path(ShortName.CONTENT_INFO).asText();
    }

    @Override
    public String getContent() {
        return cache.path(ShortName.CONTENT).asText();
    }

    @Override
    public long getContentSize() {
        return cache.path(ShortName.CONTENT_SIZE).asLong();
    }

    @Override
    protected String wrapBody(String body) {
        return wrapXmlBody(body);
    }

    protected static String wrapXmlBody(String body) {
        return "<om2m:cin xmlns:om2m=\"http://www.onem2m.org/xml/protocols\">"
                + body
                + "</om2m:cin>";
    }

    public static class Builder extends ContentInstanceBuilder {

        private final HttpResource parent;

        public Builder(HttpResource parent) {
            this.parent = parent;
        }

        @Override
        public ContentInstance create(String name) {
            String body = wrapXmlBody(
                    (this.contentInfo != null ? "<cnf>" + this.contentInfo + "</cnf>" : "") +
                            (this.content != null ? "<con>" + XmlEscapers.xmlContentEscaper().escape(this.content) + "</con>" : "") +
                            (this.labels != null ? "<lbl>" + Joiner.on(" ").join(labels) + "</lbl>" : ""));

            try {
                URI uri = parent.httpClient.buildUri(parent.getId()).build();
                Request request = Request.Post(uri)
                                         .bodyString(body, ContentType.parse("application/xml;ty=" + ResourceType.CONTENT_INSTANCE));
                if (!Strings.isNullOrEmpty(name)) {
                    request = request.addHeader("X-M2M-NM", name);
                }
                JsonNode jsonNode = parent.httpClient.executeRequestAndParse(request);
                return new HttpContentInstance(parent.node, jsonNode);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot build URI", e);
            }
        }
    }

}
