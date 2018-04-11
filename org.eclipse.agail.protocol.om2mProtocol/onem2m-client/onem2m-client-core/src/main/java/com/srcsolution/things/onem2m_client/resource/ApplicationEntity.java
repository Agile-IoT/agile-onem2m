package com.srcsolution.things.onem2m_client.resource;

import java.util.List;
import java.util.stream.Collectors;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.builder.ContainerBuilder;

public interface ApplicationEntity extends Resource {

    default ResourceType getType() {
        return ResourceType.APPLICATION_ENTITY;
    }

    String getAppName();

    String getAppId();

    String getAeId();

    List<String> getPointOfAccess();

    String getNodeLinkId();

    default List<Container> getContainers() {
        return children(ResourceType.CONTAINER).stream().map(resource -> (Container) resource).collect(Collectors.toList());
    }

    ContainerBuilder buildContainer();

}
