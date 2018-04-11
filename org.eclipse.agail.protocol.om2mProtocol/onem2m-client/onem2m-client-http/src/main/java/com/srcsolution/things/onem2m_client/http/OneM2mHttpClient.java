package com.srcsolution.things.onem2m_client.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import embedded_libs.com.google.common.base.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.ResourceException;

import static org.slf4j.LoggerFactory.getLogger;

public class OneM2mHttpClient {

    private final Node node;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static int counter = 0;

    public OneM2mHttpClient(Node node) {
        this.node = node;
    }

    public JsonNode retrieveJson(String resourceId) {
        Request request = buildGetRequest(resourceId);
        return executeRequestAndParse(request);
    }

    public URIBuilder buildUri(String path) {
        try {
            return new URIBuilder(node.getUrl() + "/~" + (path != null ? (path.startsWith("/") ? path : "/" + path) : ""));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot build URL", e);
        }
    }

    public Request buildGetRequest(String path) {
        try {
            return buildGetRequest(buildUri(path).build());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot build URL", e);
        }
    }

    public Request buildGetRequest(URI uri) {
        return buildRequest(Request.Get(uri));
    }

    public Request buildRequest(Request request) {
        return request.connectTimeout(5 * 1000)
                      .socketTimeout(2 * 60 * 1000)
                      //                      .addHeader(HttpHeaders.PRAGMA, "no-cache")
                      //                      .addHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                      //                      .addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, compress, br")
                      .addHeader(HttpHeaders.ACCEPT, "application/json")
                      .addHeader("X-M2M-Origin", buildOriginHeader());
    }

    private String buildOriginHeader() {
        return node.getUser() + ":" + node.getPassword();
    }

    public JsonNode executeRequestAndParse(Request request) throws ResourceException{
        try {
            final Request finalRequest = buildRequest(request);
            //            if (!LOGGER.isTraceEnabled()) {
            //                LOGGER.trace("Send oneM2M HTTP request : {}", request);
            //            }
            return request.execute().handleResponse(response -> {

                LOGGER.trace("Sent oneM2M HTTP request : {}, response: {}", request, response);
                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();
                if (statusLine.getStatusCode() >= 300) {
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase() + ": " + finalRequest);
                }

                if (entity.getContentLength() != 0) {
                    String content = EntityUtils.toString(entity, parseContentEncoding(entity));
                    EntityUtils.consumeQuietly(entity);
                    if (content != null && content.trim().length() > 0) {
                        JsonNode jsonNode = JSON_MAPPER.readTree(content);
                        return jsonNode.elements().next();
                    }
                }
                counter = 0;
                return NullNode.getInstance();
            });
        } catch (IOException e) {
            counter++;
            if (counter <= 5) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {

                }
                LOGGER.warn("Request error: {}, trying again...", e.getMessage(), e);
                return executeRequestAndParse(request);
            } else {
                throw new ResourceException("Request error: " + e.getMessage(), e, 0);
            }
        }
    }

    private String parseContentEncoding(HttpEntity entity) {
        try {
            String headerValue = entity.getContentType().getValue();
            String encoding = headerValue.substring(headerValue.lastIndexOf(";charset=") + 9);
            if (!Strings.isNullOrEmpty(encoding)) {
                return encoding;
            }
        } catch (Exception ignored) {
        }
        return "UTF-8"; //default
    }

    public static final Logger LOGGER = getLogger(OneM2mHttpClient.class);

}
