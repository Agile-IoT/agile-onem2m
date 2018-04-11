package com.srcsolution.things.onem2m_client.http;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import embedded_libs.com.google.common.base.MoreObjects;
import embedded_libs.com.google.common.base.Strings;
import embedded_libs.com.google.common.xml.XmlEscapers;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.eclipse.om2m.commons.constants.FilterUsage;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.constants.ResultContent;
import org.eclipse.om2m.commons.constants.ShortName;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.*;
import com.srcsolution.things.onem2m_client.resource.AbstractResource;
import com.srcsolution.things.onem2m_client.resource.NotificationContentType;
import com.srcsolution.things.onem2m_client.resource.builder.ApplicationEntityBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContentInstanceBuilder;

import static org.slf4j.LoggerFactory.getLogger;

public class HttpResource extends AbstractResource {

    protected final Node node;

    protected OneM2mHttpClient httpClient;

    protected JsonNode cache;

    protected HttpResource(Node node, String resourceId) {
        super();
        this.node = node;
        httpClient = new OneM2mHttpClient(node);
        setFrom(retrieve(resourceId));
    }

    protected HttpResource(Node node, JsonNode json) {
        super();
        this.node = node;
        httpClient = new OneM2mHttpClient(node);
        setFrom(json);
    }

    protected JsonNode retrieve(String resourceId) {
        Request request = httpClient.buildGetRequest(resourceId);
        return httpClient.executeRequestAndParse(request);
    }

    protected void setFrom(JsonNode jsonNode) {
        this.cache = jsonNode;
        setId(jsonNode.path(ShortName.RESOURCE_ID).asText());
        setParentId(jsonNode.path(ShortName.PARENT_ID).asText());
        setName(jsonNode.path(ShortName.RESOURCE_NAME).asText());
        int type = jsonNode.path(ShortName.RESOURCE_TYPE).asInt(0);
        if (type > 0) {
            setType(type);
        }
        setCreationTime(parseDate(jsonNode.path(ShortName.CREATION_TIME).asText()));
        setLastModifiedTime(parseDate(jsonNode.path(ShortName.LAST_MODIFIED_TIME).asText()));
        setLabels(parseStringArray(jsonNode.path(ShortName.LABELS)));

        // if attribute "la" is defined, get hierarchical url of this resource
        String lastChild = jsonNode.path(ShortName.LATEST).asText();
        if (!Strings.isNullOrEmpty(lastChild)) {
            setHierarchicalUrl(lastChild.substring(0, lastChild.length() - ShortName.LATEST.length() - 1));
        }
    }

    protected List<String> parseStringArray(JsonNode jsonNode) {
        List<String> labels = new ArrayList<>(jsonNode.size());
        for (JsonNode node : jsonNode) {
            labels.add(node.asText());
        }
        return labels;
    }

    @Override
    public String getAttribute(String attributeName) {
        return cache.path(attributeName).asText();
    }

    protected ZonedDateTime parseDate(String text) {return ZonedDateTime.parse(text, DATE_TIME_FORMATTER);}

    @Override
    public Optional<Resource> child(String name) {
        if (Strings.isNullOrEmpty(hierarchicalUrl)) {
            return super.child(name);
        } else {
            try {
                JsonNode jsonNode = retrieve(hierarchicalUrl + "/" + name);
                return Optional.of(create(node, jsonNode));
            } catch (ResourceException re) {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Resource> deepChild(String name, String... names) {
        if (Strings.isNullOrEmpty(hierarchicalUrl)) {
            Optional<Resource> child = child(name);
            if (names != null && names.length > 0 && child.isPresent()) {
                child = child.get().deepChild(names[0], Arrays.copyOfRange(names, 1, names.length));
            }
            return child;

        } else {
            StringBuilder n = new StringBuilder(name);
            if (names != null) {
                for (String cn : names) {
                    n.append("/").append(cn);
                }
            }
            return child(n.toString());
        }
    }

    @Override
    public List<Resource> children(ResourceType... resourceTypes) {
        String path = getId();
        if (getType() != null && org.eclipse.om2m.commons.constants.ResourceType.REMOTE_CSE == getType().getValue()) {
            path = cache.path(ShortName.CSE_ID).asText();
        }
        try {
            URI uri = httpClient.buildUri(path)
                                //                                .addParameter(ShortName.RESULT_CONTENT, String.valueOf(ResultContent.CHILD_REF)).build();
                                .addParameter(ShortName.RESULT_CONTENT, String.valueOf(ResultContent.ATTRIBUTES_AND_CHILD_RES)).build();

            JsonNode json = httpClient.executeRequestAndParse(httpClient.buildGetRequest(uri));

            final List<Resource> children = new ArrayList<>();

            //            if (json != null && json.size() > 0) {
            //                json = json.path(ShortName.CHILD_RESOURCE);
            //                if (json.isArray()) {
            //                    final List<Integer> types = Stream.of(resourceTypes).map(ResourceType::getValue).collect(Collectors.toList());
            //                    json.forEach(n -> {
            //                        if (types.isEmpty() || types.contains(n.path("ty").asInt())) {
            //                            children.add(create(node, n.path("value").asText()));
            //                        }
            //                    });
            //                }
            //            }
            if (json != null && json.size() > 0) {
                // browse each children for each requested (or all) types
                ResourceType[] types = (resourceTypes != null && resourceTypes.length > 0) ? resourceTypes : ResourceType.values();
                for (ResourceType resourceType : types) {
                    JsonNode typeChildren = json.path(resourceType.getShortName());
                    for (JsonNode typeChild : typeChildren) {
                        children.add(create(node, typeChild));
                    }
                }
            }

            return children;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot build URI", e);
        }

    }

    HttpResource create(Node node, String resourceId) {
        return create(node, retrieve(resourceId));
    }

    static HttpResource create(Node node, JsonNode json) {
        int type = json.path(ShortName.TYPE).asInt();
        switch (type) {
            case org.eclipse.om2m.commons.constants.ResourceType.AE:
                return new HttpApplicationEntity(node, json);
            case org.eclipse.om2m.commons.constants.ResourceType.CONTAINER:
                return new HttpContainer(node, json);
            case org.eclipse.om2m.commons.constants.ResourceType.CONTENT_INSTANCE:
                return new HttpContentInstance(node, json);
            case org.eclipse.om2m.commons.constants.ResourceType.CSE_BASE:
                return new HttpCseBase(node, json);
        }
        return new HttpResource(node, json);
    }

    @Override
    public List<Resource> find(ResourceType type, String... labels) {
        try {
            URIBuilder uriBuilder = httpClient.buildUri(getId())
                                              .addParameter(ShortName.FILTER_USAGE, String.valueOf(FilterUsage.DISCOVERY_CRITERIA));

            if (type != null) {
                uriBuilder.addParameter(ShortName.RESOURCE_TYPE, String.valueOf(type.getValue()));
            }
            if (labels != null) {
                for (String label : labels) {
                    uriBuilder.addParameter(ShortName.LABELS, label);
                }
            }
            JsonNode json = httpClient.executeRequestAndParse(httpClient.buildGetRequest(uriBuilder.build()));
            if (json.isTextual()) {
                List<Resource> set = new ArrayList<>();
                for (String resourceId : json.asText().split(" ")) {
                    try {
                        set.add(create(node, resourceId));
                    } catch (Exception e) {
                        LOGGER.warn("Cannot add found resource " + resourceId, e);
                    }
                }
                return set;
            }

            return Collections.emptyList();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot build URI", e);
        }
    }

    @Override
    public Set<Resource> deepFind(ResourceType type, String... labels) {
        Set<Resource> cseResources = new HashSet<>();
        deepFind(cseResources, new HashSet<>(), type, labels);
        return cseResources;
    }

    private void deepFind(Set<Resource> results, Set<String> cseResources, ResourceType resourceType, String... labels) {
        results.addAll(find(resourceType, labels));

        Collection<Resource> remoteCseResources = find(ResourceType.REMOTE_CSE);
        for (Resource remoteCseResource : remoteCseResources) {
            try {
                String uri = remoteCseResource.getAttribute(ShortName.CSE_ID);
                if (!cseResources.contains(uri)) {
                    cseResources.add(uri);

                    Node remoteNode = new Node(this.node.getUrl(), uri, this.node.getUser(), this.node.getPassword());
                    HttpResourceFactory httpResourceFactory = new HttpResourceFactory(remoteNode);
                    HttpResource remoteNodeRoot = httpResourceFactory.retrieveRoot();
                    remoteNodeRoot.deepFind(results, cseResources, resourceType, labels);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <R extends ResourceBuilder> R buildChild(Class<? extends R> builder) {
        if (builder != null) {
            if (ApplicationEntityBuilder.class.isAssignableFrom(builder)) {
                return (R) new HttpApplicationEntity.Builder(this);

            } else if (ContainerBuilder.class.isAssignableFrom(builder)) {
                return (R) new HttpContainer.Builder(this);

            } else if (ContentInstanceBuilder.class.isAssignableFrom(builder)) {
                return (R) new HttpContentInstance.Builder(this);
            }
        }
        throw new IllegalArgumentException("No builder found");
    }

    @Override
    public Resource updateAttribute(String name, Serializable value) {
        throw new UnsupportedOperationException("not yet");
    }

    @Override
    public void delete() {
        try {
            Request request = Request.Delete(httpClient.buildUri(getId()).build());
            httpClient.executeRequestAndParse(request);
            LOGGER.trace("Resource {} deleted", id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot build URI", e);
        }
    }

    @Override
    public void touch() {
        try {
            URI uri = httpClient.buildUri(getId()).build();
            Request request = Request.Put(uri)
                                     .addHeader("X-M2M-NM", name)
                                     .bodyString(wrapBody(""), ContentType.parse("application/xml;ty=" + this.getType().getValue()));
            httpClient.executeRequestAndParse(request);
            LOGGER.trace("Resource {} touched", id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot build URI", e);
        }
    }

    protected String wrapBody(String body) {
        throw new UnsupportedOperationException("not yet");
    }

    @Override
    public void subscribe(String subscriptionName, String callbackUrl, NotificationContentType notificationContentType) {
        try {
            Optional<Resource> subscriptionResource = child(subscriptionName);

            String resourceNameAttribute;
            Request request;
            if (subscriptionResource.isPresent()) {
                Resource resource = subscriptionResource.get();
                if (!resource.getType().equals(ResourceType.SUBSCRIPTION)) {
                    throw new ResourceException("Cannot subscribe to resource " + id
                                                        + " because another child resource " + subscriptionName + " already exists",
                                                ResponseStatusCode.ALREADY_EXISTS.intValue());
                }
                URI uri = httpClient.buildUri(resource.getId()).build();
                request = Request.Put(uri);
                resourceNameAttribute = "";

            } else {
                URI uri = httpClient.buildUri(getId()).build();
                request = Request.Post(uri);
                resourceNameAttribute = MessageFormat.format("rn=\"{0}\"", XmlEscapers.xmlAttributeEscaper().escape(subscriptionName));
            }

            int nct = MoreObjects.firstNonNull(notificationContentType, NotificationContentType.MODIFIED_ATTRIBUTES).getValue();

            String body = MessageFormat.format("<m2m:sub xmlns:m2m=\"http://www.onem2m.org/xml/protocols\" {0}>" +
                                                       "<nu>{1}</nu>" +
                                                       "<nct>{2}</nct>" +
                                                       "</m2m:sub>",
                                               resourceNameAttribute,
                                               XmlEscapers.xmlContentEscaper().escape(callbackUrl),
                                               nct
            );

            request = request.addHeader("X-M2M-NM", subscriptionName)
                             .bodyString(body, ContentType.parse("application/xml;ty=" + ResourceType.SUBSCRIPTION.getValue()));
            httpClient.executeRequestAndParse(request);
            //LOGGER.trace("Resource {} subscribed", id);

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot create subscription", e);
        }
    }

    public static final Logger LOGGER = getLogger(HttpResource.class);

}
