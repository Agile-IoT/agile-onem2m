package org.eclipse.agail.protocol.om2m.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpHeaders;
import org.eclipse.om2m.commons.resource.RequestPrimitive;

public class Request {

    private final String resourceId;

    private final String path;

    private final Map<String, List<String>> queryParams;

    private final String body;

    private final String method;

    private final String contentType;

    private final String accept;

    public Request(HttpServletRequest servletRequest, String resourceId) throws IOException {
        this.resourceId = resourceId;
        path = servletRequest.getPathInfo();
        method = servletRequest.getMethod();
        queryParams = splitQueryString(servletRequest.getQueryString());
        body = readerToString(servletRequest.getReader());
        contentType = servletRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        accept = servletRequest.getHeader(HttpHeaders.ACCEPT);
    }

    public Request(RequestPrimitive requestPrimitive) {
        resourceId = requestPrimitive.getTo();
        path = "";
        method = parseMethod(requestPrimitive);
        queryParams = requestPrimitive.getQueryStrings();
        body = (String) requestPrimitive.getContent();
        contentType = requestPrimitive.getRequestContentType();
        accept = requestPrimitive.getReturnContentType();
    }

    private Map<String, List<String>> splitQueryString(String query) {
        Map<String, List<String>> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                try {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = URLDecoder.decode(pair[1], "UTF-8");
                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<>();
                        values.add(value);
                    }
                    params.put(key, values);
                } catch (UnsupportedEncodingException e) {
                    //NOOP
                }
            }
        }
        return params;
    }

    private String parseMethod(RequestPrimitive requestPrimitive) {
        String method;
        switch (requestPrimitive.getOperation().intValue()) {
            case 1:
                method = "POST";
                break;
            case 2:
                method = "GET";
                break;
            case 3:
                method = "UPDATE";
                break;
            case 4:
                method = "DELETE";
                break;
            case 5:
                method = "POST";
                break;
            default:
                method = "GET";
        }
        return method;
    }

    public String readerToString(Reader initialReader)
            throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = initialReader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        initialReader.close();
        return buffer.toString();
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public String getContentType() {
        return contentType;
    }

    public String getAccept() {
        return accept;
    }

    public String getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", queryParams=" + queryParams +
                '}';
    }

    // REQUEST MATCHING

    private static AntPathMatcher matcher = new AntPathMatcher();

    public boolean matches(String pathPattern) {
        return matcher.match(pathPattern, path);
    }

    public boolean matches(String pathPattern, String httpMethod) {
        return httpMethod != null && !httpMethod.isEmpty()
                && httpMethod.equalsIgnoreCase(method)
                && matches(pathPattern);
    }

    public boolean matchesOperation(String operationName, String httpMethod) {
        List<String> op = queryParams.get("op");
        if (op != null && op.size() == 1) {
            if (operationName != null) {
                if (operationName.equalsIgnoreCase(op.get(0)) && httpMethod.equalsIgnoreCase(getMethod())) {
                    return true;
                }
            }
        }
        return false;
    }

    public UUID getToken() {
        try {
            return UUID.fromString(getQueryParams().get("token").get(0));
        } catch (Exception e) {
            return null;
        }
    }
}
