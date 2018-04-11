package com.srcsolution.things.onem2m_client.osgi;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.constants.ResultContent;
import org.eclipse.om2m.commons.resource.*;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.ResourceException;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.AbstractResource;
import com.srcsolution.things.onem2m_client.resource.NotificationContentType;
import com.srcsolution.things.onem2m_client.resource.builder.*;

import static org.slf4j.LoggerFactory.getLogger;

public class OsgiResource extends AbstractResource {

    public static final Logger LOGGER = getLogger(OsgiResource.class);

    protected final RequestSender requestSender;

    protected org.eclipse.om2m.commons.resource.Resource om2mResource;

    protected OsgiResource(RequestSender requestSender, String resourceId) {
        super();
        this.requestSender = requestSender;
        setFrom(retrieve(resourceId));
    }

    protected OsgiResource(RequestSender requestSender, org.eclipse.om2m.commons.resource.Resource om2mResource) {
        super();
        this.requestSender = requestSender;
        setFrom(om2mResource);
    }

    private org.eclipse.om2m.commons.resource.Resource retrieve(String resourceId) {
        ResponsePrimitive response = requestSender.retrieve(resourceId);
        checkStatus(response, ResponseStatusCode.OK);
        return (org.eclipse.om2m.commons.resource.Resource) response.getContent();
    }

    protected ResponsePrimitive checkStatus(ResponsePrimitive response, BigInteger expectedStatus) {
        if (expectedStatus.equals(response.getResponseStatusCode())) {
            return response;
        } else {
            int code = response.getResponseStatusCode().intValue();
            throw new ResourceException("Status code " + code + " invalid (" + expectedStatus + " expected)", code);
        }
    }

    private void setFrom(org.eclipse.om2m.commons.resource.Resource om2mResource) {
        this.om2mResource = om2mResource;
        setId(om2mResource.getResourceID());
        setParentId(om2mResource.getParentID());
        setName(om2mResource.getName());
        if (om2mResource.getResourceType() != null) {
            setType(om2mResource.getResourceType().intValue());
        }
        setCreationTime(ZonedDateTime.parse(om2mResource.getCreationTime(), DATE_TIME_FORMATTER));
        setLastModifiedTime(ZonedDateTime.parse(om2mResource.getLastModifiedTime(), DATE_TIME_FORMATTER));
        setLabels(om2mResource.getLabels());
    }

    private OsgiResource create(RequestSender requestSender, String resourceId) {
        return create(requestSender, retrieve(resourceId));
    }

    static OsgiResource create(RequestSender requestSender, org.eclipse.om2m.commons.resource.Resource resource) {
        if (resource.getResourceType() != null) {
            switch (resource.getResourceType().intValue()) {
                case org.eclipse.om2m.commons.constants.ResourceType.AE:
                    return new OsgiApplicationEntity(requestSender, resource);
                case org.eclipse.om2m.commons.constants.ResourceType.CONTAINER:
                    return new OsgiContainer(requestSender, resource);
                case org.eclipse.om2m.commons.constants.ResourceType.CONTENT_INSTANCE:
                    return new OsgiContentInstance(requestSender, resource);
                case org.eclipse.om2m.commons.constants.ResourceType.CSE_BASE:
                    return new OsgiCseBase(requestSender, resource);
            }
        }
        return new OsgiResource(requestSender, resource);
    }

    @Override
    public List<Resource> children(ResourceType... types) {
        ResponsePrimitive response = requestSender.retrieve(id, ResultContent.ATTRIBUTES_AND_CHILD_REF);
        checkStatus(response, ResponseStatusCode.OK);
        List<ChildResourceRef> childrenRef = listChildrenRef((org.eclipse.om2m.commons.resource.Resource) response.getContent());

        return childrenRef.stream()
                          .filter(ref -> types == null || types.length == 0
                                  || Arrays.stream(types)
                                           .anyMatch(type -> type.getValue() == ref.getType().intValue()))
                          .map(ref -> create(requestSender, ref.getValue()))
                          .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<Resource> deepChild(String name, String... names) {
        Optional<Resource> child = child(name);
        if (names != null && names.length > 0 && child.isPresent()) {
            child = child.get().deepChild(names[0], Arrays.copyOfRange(names, 1, names.length));
        }
        return child;
    }

    @SuppressWarnings ("unchecked")
    protected List<ChildResourceRef> listChildrenRef(org.eclipse.om2m.commons.resource.Resource om2mResource) {
        if (type != null) {
            switch (type) {
                case APPLICATION_ENTITY:
                    return ((AE) om2mResource).getChildResource();
                case CONTAINER:
                    return ((Container) om2mResource).getChildResource();
                case CSE_BASE:
                    return ((CSEBase) om2mResource).getChildResource();
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<Resource> find(ResourceType type, String... labels) {
        List<Resource> results = new ArrayList<>();
        FilterCriteria fc = new FilterCriteria();
        if (type != null) {
            fc.setResourceType(BigInteger.valueOf(type.getValue()));
        }
        if (labels != null && labels.length > 0) {
            for (String lbl : labels) {
                fc.getLabels().add(lbl);
            }
        }
        ResponsePrimitive response = requestSender.discover(id, fc);
        List<String> list = ((URIList) response.getContent()).getListOfUri();
        for (String uri : list) {
            results.add(create(requestSender, uri));
        }
        return results;
    }

    @Override
    public Set<Resource> deepFind(ResourceType type, String... labels) {
        throw new UnsupportedOperationException("not yet");
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <R extends ResourceBuilder> R buildChild(Class<? extends R> builder) {
        if (builder != null) {
            if (ApplicationEntityBuilder.class.isAssignableFrom(builder)) {
                return (R) new OsgiApplicationEntity.Builder(this);

            } else if (ContainerBuilder.class.isAssignableFrom(builder)) {
                return (R) new OsgiContainer.Builder(this);

            } else if (ContentInstanceBuilder.class.isAssignableFrom(builder)) {
                return (R) new OsgiContentInstance.Builder(this);

            }/* else if (NodeBuilder.class.isAssignableFrom(builder)) {
                return (R) new OsgiNode.Builder(this);

            } else if (MgmtObjBuilder.DeviceInfo.class.isAssignableFrom(builder)) {
                return (R) new OsgiMgmtObj.OsgiDeviceInfo.Builder(this);
            }*/
        }
        throw new IllegalArgumentException("No builder found");
    }

    @Override
    public Resource updateAttribute(String name, Serializable value) {
        throw new UnsupportedOperationException("not yet");
    }

    @Override
    public void delete() {
        ResponsePrimitive response = requestSender.deleteResource(id);
        checkStatus(response, ResponseStatusCode.DELETED);
    }

    @Override
    public void touch() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String getAttribute(String name) {
        throw new UnsupportedOperationException("not yet");
    }

    @Override
    public void subscribe(String subscriptionName, String callbackUrl, NotificationContentType notificationContentType) {
        throw new UnsupportedOperationException("not yet");
    }

}
