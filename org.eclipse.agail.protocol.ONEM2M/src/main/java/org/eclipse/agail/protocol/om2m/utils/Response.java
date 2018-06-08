package org.eclipse.agail.protocol.om2m.utils;

import java.io.IOException;
import java.math.BigInteger;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import static javax.servlet.http.HttpServletResponse.*;

public class Response {

    private final HttpServletResponse servletResponse;

    private final ResponsePrimitive responsePrimitive;

    private int status;

    private String content;

    private String contentType;

    public Response(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
        this.responsePrimitive = null;
    }

    public Response(ResponsePrimitive responsePrimitive) {
        this.responsePrimitive = responsePrimitive;
        this.servletResponse = null;
    }

    public int getStatus() {
        return status;
    }

    public Response setStatus(int status) {
        this.status = status;
        if (responsePrimitive != null) {
            BigInteger code;
            switch (status) {
                case SC_BAD_REQUEST:
                    code = ResponseStatusCode.BAD_REQUEST;
                    break;
                case SC_CREATED:
                    code = ResponseStatusCode.CREATED;
                    break;
                case SC_INTERNAL_SERVER_ERROR:
                    code = ResponseStatusCode.INTERNAL_SERVER_ERROR;
                    break;
                case SC_OK:
                    code = ResponseStatusCode.OK;
                    break;
                case SC_NOT_FOUND:
                    code = ResponseStatusCode.NOT_FOUND;
                    break;
                case SC_REQUEST_TIMEOUT:
                    code = ResponseStatusCode.REQUEST_TIMEOUT;
                    break;
                default:
                    code = BigInteger.valueOf(status);
            }
            responsePrimitive.setResponseStatusCode(code);

        } else if (servletResponse != null) {
            servletResponse.setStatus(status);
        }
        return this;
    }

    public Response setStatus(BigInteger status) {
        this.status = status.intValue();
        if (responsePrimitive != null) {
            responsePrimitive.setResponseStatusCode(status);
        } else if (servletResponse != null) {
            servletResponse.setStatus(this.status);
        }
        return this;
    }

    public String getContent() {
        return content;
    }

    public Response setContent(String content) throws IOException {
        this.content = content;
        if (responsePrimitive != null) {
            responsePrimitive.setContent(content);

        } else if (servletResponse != null) {
            servletResponse.getWriter().write(content);
        }
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public Response setContentType(String contentType) {
        this.contentType = contentType;
        if (responsePrimitive != null) {
            responsePrimitive.setContentType(contentType);

        } else if (servletResponse != null) {
            servletResponse.setContentType(contentType);
        }
        return this;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "status=" + status +
                '}';
    }

}
