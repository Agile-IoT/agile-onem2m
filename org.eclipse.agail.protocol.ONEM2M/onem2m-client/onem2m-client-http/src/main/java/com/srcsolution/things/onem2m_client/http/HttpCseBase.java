package com.srcsolution.things.onem2m_client.http;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.om2m.commons.constants.ShortName;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.resource.CseBase;
import com.srcsolution.things.onem2m_client.resource.CseType;

public class HttpCseBase extends HttpResource implements CseBase {

    protected HttpCseBase(Node node, String resourceId) {
        super(node, resourceId);
    }

    protected HttpCseBase(Node node, JsonNode json) {
        super(node, json);
    }

    @Override
    public CseType getCseType() {
        return CseType.valueOf(cache.path(ShortName.CSE_TYPE).asInt());
    }

    @Override
    public String getCseId() {
        return cache.path(ShortName.CSE_ID).asText();
    }

    @Override
    public List<String> getPointOfAccess() {
        return parseStringArray(cache.path(ShortName.POA));
    }

    @Override
    public String getNodeLinkId() {
        return cache.path(ShortName.NODE_LINK).asText();
    }

}
