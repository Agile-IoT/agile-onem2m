package com.srcsolution.things.onem2m_client.osgi;

import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.*;
import org.eclipse.om2m.commons.resource.*;
import org.eclipse.om2m.core.service.CseService;

public class RequestSender {

    private CseService cseService;

    protected RequestSender(CseService cseService) {this.cseService = cseService;}

    private RequestPrimitive makeRequestPrimitive(String targetId) {
        RequestPrimitive request = new RequestPrimitive();
        request.setFrom(getAdminRequestingEntity());
        request.setTargetId(targetId);
        request.setReturnContentType(MimeMediaType.OBJ);
        request.setRequestContentType(MimeMediaType.OBJ);
        return request;
    }

    private String getAdminRequestingEntity() {
        return Constants.ADMIN_REQUESTING_ENTITY;
    }

    public ResponsePrimitive retrieve(String targetId) {
        return retrieve(targetId, null);
    }

    public ResponsePrimitive retrieve(String targetId, BigInteger resultContent) {
        RequestPrimitive request = makeRequestPrimitive(targetId);
        request.setOperation(Operation.RETRIEVE);
        request.setResultContent(resultContent);
        return cseService.doRequest(request);
    }

    public ResponsePrimitive discover(String targetId, FilterCriteria criteria) {
        RequestPrimitive request = makeRequestPrimitive(targetId);
        request.setOperation(Operation.RETRIEVE);
        criteria.setFilterUsage(FilterUsage.DISCOVERY_CRITERIA);
        request.setFilterCriteria(criteria);
        return cseService.doRequest(request);
    }

    public ResponsePrimitive createResource(String targetId, String name, Resource resource, int resourceType) {
        RequestPrimitive request = new RequestPrimitive();
        request.setFrom(getAdminRequestingEntity());
        request.setTargetId(targetId);
        request.setResourceType(BigInteger.valueOf(resourceType));
        request.setRequestContentType(MimeMediaType.OBJ);
        request.setReturnContentType(MimeMediaType.OBJ);
        request.setContent(resource);
        request.setName(name);
        request.setOperation(Operation.CREATE);
        return cseService.doRequest(request);
    }

    public ResponsePrimitive createAE(AE resource, String name) {
        return createResource("/" + Constants.CSE_ID, name, resource, ResourceType.AE);
    }

    public ResponsePrimitive createContainer(String targetId, String name, Container resource) {
        return createResource(targetId, name, resource, ResourceType.CONTAINER);
    }

    public ResponsePrimitive createContentInstance(String targetId, String name, ContentInstance resource) {
        return createResource(targetId, name, resource, ResourceType.CONTENT_INSTANCE);
    }

    public ResponsePrimitive createContentInstance(String targetId, ContentInstance resource) {
        return createContentInstance(targetId, null, resource);
    }

    public ResponsePrimitive deleteResource(String targetId) {
        RequestPrimitive request = new RequestPrimitive();
        request.setFrom(getAdminRequestingEntity());
        request.setTargetId(targetId);
        request.setOperation(Operation.DELETE);
        return cseService.doRequest(request);
    }

    public ResponsePrimitive updateRessource(String targetId, Resource resource) {
        RequestPrimitive request = new RequestPrimitive();
        request.setFrom(getAdminRequestingEntity());
        request.setTargetId(targetId);
        request.setOperation(Operation.UPDATE);
        request.setContent(resource);
        request.setRequestContentType(MimeMediaType.OBJ);
        request.setReturnContentType(MimeMediaType.OBJ);
        return cseService.doRequest(request);
    }

}