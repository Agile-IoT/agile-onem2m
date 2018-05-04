package com.srcsolution.things.interworking.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import embedded_libs.com.google.common.base.MoreObjects;
import org.eclipse.om2m.commons.constants.ShortName;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.Container;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;

import static com.srcsolution.things.interworking.mapper.ThingResourcesConstants.*;
import static org.slf4j.LoggerFactory.getLogger;

public class ThingResources {

    public static final Logger LOGGER = getLogger(ThingResources.class);

    private final Container thingRootContainer;

    private Container thingPropsContainer;

    private Container deviceContainer;

    private Container connectivityContainer;

    private Container operationsContainer;

    private Container messagesContainer;

    private HashMap<String, Container> topicContainers;

    public ThingResources(Container thingResource) {
        this.thingRootContainer = thingResource;
    }

    public Container getThingRootContainer() {
        return thingRootContainer;
    }

    public Container getThingPropsContainer() {
        if (thingPropsContainer == null) {
            thingPropsContainer = (Container) thingRootContainer
                    .deepChild(PROPERTIES_RESOURCE_NAME, THING_RESOURCE_NAME).orElse(null);
        }
        return thingPropsContainer;
    }

    public ContentInstance getLastThingPropsInstance() {
        if (thingPropsContainer != null) {
            return thingPropsContainer.getLatestInstance();
        } else {
            return (ContentInstance) thingRootContainer.deepChild(PROPERTIES_RESOURCE_NAME, THING_RESOURCE_NAME, ShortName.LATEST).orElse(null);
        }
    }

    public Container getDeviceContainer() {
        if (deviceContainer == null) {
            deviceContainer = (Container) thingRootContainer
                    .deepChild(PROPERTIES_RESOURCE_NAME, DEVICE_RESOURCE_NAME).orElse(null);
        }
        return deviceContainer;
    }

    public ContentInstance getLastDeviceInstance() {
        if (deviceContainer != null) {
            return deviceContainer.getLatestInstance();
        } else {
            return (ContentInstance) thingRootContainer
                    .deepChild(PROPERTIES_RESOURCE_NAME, DEVICE_RESOURCE_NAME, ShortName.LATEST).orElse(null);
        }
    }

    public Container getConnectivityContainer() {
        if (connectivityContainer == null) {
            connectivityContainer = (Container) thingRootContainer
                    .deepChild(PROPERTIES_RESOURCE_NAME, CONNECTIVITY_RESOURCE_NAME).orElse(null);
        }
        return connectivityContainer;
    }

    public ContentInstance getLastConnectivityInstance() {
        if (connectivityContainer != null) {
            return connectivityContainer.getLatestInstance();
        } else {
            return (ContentInstance) thingRootContainer
                    .deepChild(PROPERTIES_RESOURCE_NAME, CONNECTIVITY_RESOURCE_NAME, ShortName.LATEST).orElse(null);
        }
    }

    public Container getOperationsContainer() {
        if (operationsContainer == null) {
            operationsContainer = (Container) thingRootContainer.child(OPERATIONS_RESOURCE_NAME).orElse(null);
        }
        return operationsContainer;
    }

    public ContentInstance getLastOperationsInstance() {
        if (operationsContainer != null) {
            return connectivityContainer.getLatestInstance();
        } else {
            return (ContentInstance) thingRootContainer
                    .deepChild(PROPERTIES_RESOURCE_NAME, OPERATIONS_RESOURCE_NAME, ShortName.LATEST).orElse(null);
        }
    }

    public Container getMessagesContainer() {
        if (messagesContainer == null) {
            messagesContainer = (Container) thingRootContainer.child(MESSAGE_RESOURCE_NAME).orElse(null);
        }
        return messagesContainer;
    }

    public HashMap<String, Container> getTopicContainers() {
        if (topicContainers == null) {
            topicContainers = new HashMap<>();
            getMessagesContainer().children(ResourceType.CONTAINER)
                                  .forEach(resource -> topicContainers.put(resource.getName(), (Container) resource));
        }
        return topicContainers;
    }

    public Container getTopicContainer(String name) {
        return getTopicContainers().get(name);
    }

    public List<ContentInstance> getMessages(String topic) {
        return getTopicContainer(topic).children(ResourceType.CONTENT_INSTANCE)
                                       .stream().map(ContentInstance.class::cast).collect(Collectors.toList());
    }

    public ContentInstance getLastMessageInstance(String topic) {
        Container topicContainer = getTopicContainer(topic);
        if (topicContainer != null) {
            return topicContainer.getLatestInstance();
        } else {
            return (ContentInstance) thingRootContainer
                    .deepChild(MESSAGE_RESOURCE_NAME, topic, ShortName.LATEST).orElse(null);
        }
    }

    public ThingResources setThingPropsContainer(Container thingPropsContainer) {
        this.thingPropsContainer = thingPropsContainer;
        return this;
    }

    public ThingResources setDeviceContainer(Container deviceContainer) {
        this.deviceContainer = deviceContainer;
        return this;
    }

    public ThingResources setConnectivityContainer(Container connectivityContainer) {
        this.connectivityContainer = connectivityContainer;
        return this;
    }

    public ThingResources setOperationsContainer(Container operationsContainer) {
        this.operationsContainer = operationsContainer;
        return this;
    }

    public ThingResources setMessagesContainer(Container messagesContainer) {
        this.messagesContainer = messagesContainer;
        return this;
    }

    public ThingResources setTopicContainer(String topicName, Container topicContainer) {
        if (topicContainers == null) {
            topicContainers = new HashMap<>();
        }
        topicContainers.put(topicName, topicContainer);
        return this;
    }

    // CREATE THING RESOURCES

    public static ThingResources createThingResources(ApplicationEntity rootAe, String thingId) {
        String thingResourceName = makeThingResourceName(thingId);
        Container thingResource = (Container) rootAe.child(thingResourceName)
                                                    .orElseGet(() -> {
                                                        LOGGER.debug("Thing resource {} not found, so create it", thingResourceName);
                                                        return rootAe.buildContainer()
                                                                     .addLabels(THING_RESOURCE_LABEL)
                                                                     .setMaxNumberOfIntance(0L)
                                                                     .create(thingResourceName);
                                                    });

        ThingResources thingResources = new ThingResources(thingResource);
        getOrCreateResources(thingResources);
        return thingResources;
    }

    private static void getOrCreateResources(ThingResources thingResources) {
        Container thingResource = thingResources.getThingRootContainer();

        thingResources.setMessagesContainer(getOrCreateChildContainer(thingResource, MESSAGE_RESOURCE_NAME, 0L));
        thingResources.setOperationsContainer(getOrCreateChildContainer(thingResource, OPERATIONS_RESOURCE_NAME, 1L));

        Container propertiesResource = getOrCreateChildContainer(thingResource, PROPERTIES_RESOURCE_NAME, 0L);
        thingResources.setThingPropsContainer(getOrCreateChildContainer(propertiesResource, THING_RESOURCE_NAME, 10L));
        thingResources.setConnectivityContainer(getOrCreateChildContainer(propertiesResource, CONNECTIVITY_RESOURCE_NAME, 10L));
        thingResources.setDeviceContainer(getOrCreateChildContainer(propertiesResource, DEVICE_RESOURCE_NAME, 10L));

        LOGGER.trace("Thing resources structure created for {}", thingResource.getName());
    }

    public static Container getOrCreateChildContainer(Container parent, String resourceName, Long max) {
        return (Container) parent.child(resourceName)
                                 .orElseGet(() -> parent.buildContainer()
                                                        .setMaxNumberOfIntance(max)
                                                        .create(resourceName));
    }

    public static String makeThingResourceName(String thingId) {
        return THING_RESOURCE_NAME_PREFIX + thingId;
    }

    public static String parseThingIdFromResourceName(String name) {
        return name.replaceFirst(THING_RESOURCE_NAME_PREFIX, "");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", thingRootContainer.getName())
                          .add("id", thingRootContainer.getId())
                          .toString();
    }
}
