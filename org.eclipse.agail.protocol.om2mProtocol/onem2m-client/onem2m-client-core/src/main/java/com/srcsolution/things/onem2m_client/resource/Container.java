package com.srcsolution.things.onem2m_client.resource;

import java.util.List;
import java.util.stream.Collectors;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;
import com.srcsolution.things.onem2m_client.resource.builder.ContentInstanceBuilder;

public interface Container extends Resource {

    default ResourceType getType() {
        return ResourceType.CONTAINER;
    }

    default List<Container> getContainers() {
        return children(ResourceType.CONTAINER).stream().map(child -> (Container) child).collect(Collectors.toList());
    }

    default List<ContentInstance> getContentInstances() {
        return children(ResourceType.CONTENT_INSTANCE).stream().map(child -> (ContentInstance) child).collect(Collectors.toList());
    }

    ContentInstance getLatestInstance();

    ContentInstance getOldestInstance();

    long getMaxNumberOfIntance();

    long getCurrentNumberOfIntance();

    ContainerBuilder buildContainer();

    ContentInstanceBuilder buildContentInstance();

}
