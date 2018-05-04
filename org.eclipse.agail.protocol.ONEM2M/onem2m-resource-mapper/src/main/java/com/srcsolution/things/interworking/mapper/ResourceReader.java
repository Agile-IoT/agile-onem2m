package com.srcsolution.things.interworking.mapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.srcsolution.things.interworking.data.*;
import com.srcsolution.things.interworking.mapper.content.ContentConverters;
import com.srcsolution.things.interworking.mapper.content.OperationContent;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.Container;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;

import static com.srcsolution.things.interworking.mapper.ThingResourcesConstants.*;
import static org.slf4j.LoggerFactory.getLogger;

public class ResourceReader {

    public static final Logger LOGGER = getLogger(ResourceReader.class);

    private final ApplicationEntity rootAe;

    public ResourceReader(ApplicationEntity rootAe) {this.rootAe = rootAe;}

    public ApplicationEntity getRootAe() {
        return rootAe;
    }

    public List<Container> listThingResources() {
        LOGGER.debug("List things from application resource {}", rootAe);
        return rootAe.children(ResourceType.CONTAINER).stream()
                     .filter(resource -> resource.getLabels().contains(THING_RESOURCE_LABEL))
                     .map(Container.class::cast).collect(Collectors.toList());
    }

    public Map<String, ThingResources> mapThingResources() {
        Map<String, ThingResources> resources = new HashMap<>();
        listThingResources().forEach(container -> {
            LOGGER.debug("Map thing resources for {}", container.getName());
            resources.put(container.getName(), new ThingResources(container));
        });
        return resources;
    }

    public Thing readThingFully(ThingResources thingResources) {
        Thing thingData = readThing(thingResources);
        try {
            thingData.setDevice(readDevice(thingResources));
        } catch (Exception ignored) {
        }
        try {
            thingData.setConnectivity(readConnectivity(thingResources));
        } catch (Exception ignored) {
        }
        Map<String, Topic> topicDataMap = readTopicsWithMessages(thingResources);
        thingData.setTopics(new ArrayList<>(topicDataMap.values()));
        return thingData;
    }

    public Thing readThing(ThingResources resources) {
        LOGGER.debug("Read thing from resources {}", resources);
        ContentInstance resource = resources.getLastThingPropsInstance();
        Map<String, Object> content = ContentConverters.read(resource);

        Thing thing = new Thing();
        thing.setId((String) content.get(THING_ID_ATTRIBUTE));
        thing.setName((String) content.get(THING_NAME_ATTRIBUTE));

        return thing;
    }

    public Device readDevice(ThingResources resources) {
        LOGGER.debug("Read device from resources {}", resources);
        ContentInstance deviceInstance = resources.getLastDeviceInstance();
        Map<String, Object> content = ContentConverters.read(deviceInstance);

        Device device = new Device();
        content.forEach(device.getAdditionalProperties()::put);
        device.setId((String) content.get(DEVICE_ID_ATTRIBUTE));
        device.setName((String) content.get(DEVICE_NAME_ATTRIBUTE));
        device.setProductName((String) content.get(DEVICE_PRODUCT_NAME_ATTRIBUTE));
        device.setManufacturerName((String) content.get(DEVICE_MANUFACTURER_NAME_ATTRIBUTE));
        device.setReference((String) content.get(DEVICE_REFERENCE_ATTRIBUTE));
        device.setSerialNumber((String) content.get(DEVICE_SERIAL_NUMBER_ATTRIBUTE));
        device.setBatteryStatus(DeviceBatteryStatus.valueOf((String) content.get(DEVICE_BATTERY_STATUS_ATTRIBUTE)));
        device.setBatteryLevel((Integer) content.get(DEVICE_BATTERY_LEVEL_ATTRIBUTE));

        return device;
    }

    public Connectivity readConnectivity(ThingResources resources) {
        LOGGER.debug("Read connectivity from resources {}", resources);
        ContentInstance connectivityInstance = resources.getLastConnectivityInstance();
        Map<String, Object> content = ContentConverters.read(connectivityInstance);

        Connectivity connectivity = new Connectivity();
        content.forEach(connectivity.getAdditionalProperties()::put);
        connectivity.setId((String) content.get(CONNECTIVITY_ID_ATTRIBUTE));
        connectivity.setRawStatus((String) content.get(CONNECTIVITY_RAW_STATUS_ATTRIBUTE));
        connectivity.setNetworkType((String) content.get(CONNECTIVITY_NETWORK_TYPE_ATTRIBUTE));
        Object cs = content.get(CONNECTIVITY_STATUS_ATTRIBUTE);
        if (cs != null) {
            connectivity.setStatus(ConnectivityStatus.valueOf((String) cs));
        }

        return connectivity;
    }

    public Set<OperationData> readOperations(ThingResources resources) {
        LOGGER.debug("Read operations from resources {}", resources);
        ContentInstance operationInstance = resources.getLastOperationsInstance();
        Map<String, Object> content = ContentConverters.read(operationInstance);

        Set<OperationData> operations = new HashSet<>();
        for (Map.Entry<String, ?> operationData : content.entrySet()) {
            try {
                operations.add(((OperationContent) operationData.getValue()).getOperation());
            } catch (Exception e) {
                LOGGER.warn("Unexpected error while reading operation {}.", operationData.getKey(), e);
            }
        }
        return operations;
    }

    public List<Topic> readTopics(ThingResources resources) {
        LOGGER.debug("Read topics from resources {}", resources);
        HashMap<String, Container> topicContainers = resources.getTopicContainers();
        return topicContainers.entrySet().stream().map(entry -> new Topic(entry.getKey())).collect(Collectors.toList());
    }

    public Map<String, Topic> readTopicsWithMessages(ThingResources resources) {
        return readTopics(resources).stream()
                                    .peek(topic -> topic.setMessages(readMessages(topic.getName(), resources)))
                                    .collect(Collectors.toMap(Topic::getName, Function.identity()));
    }

    public List<Message> readMessages(String topicName, ThingResources resources) {
        LOGGER.debug("Read messages of topic {} from resources {}", topicName, resources);
        List<ContentInstance> messages = resources.getMessages(topicName);

        return messages.stream().map(cin -> readMessage(topicName, cin))
                       .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Message readMessage(String topicName, ContentInstance cin) {
        Message message = null;
        try {
            Map<String, Object> content = ContentConverters.read(cin);
            message = new Message();

            String timestampString = (String) content.get(MESSAGE_TIMESTAMP_ATTRIBUTE);
            if (timestampString.contains("Z")) {
                timestampString = timestampString.substring(0, timestampString.indexOf('Z') + 1);
            } else if (timestampString.contains("[")) {
                timestampString = timestampString.substring(0, timestampString.indexOf('['));
            }
            message.setTimestamp(ZonedDateTime.parse(timestampString, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            message.setPayload((String) content.get(MESSAGE_PAYLOAD_ATTRIBUTE));
            message.setLatitude((Double) content.get(MESSAGE_LATITUDE_ATTRIBUTE));
            message.setLongitude((Double) content.get(MESSAGE_LONGITUDE_ATTRIBUTE));
            message.setMetadata((Map<String, Object>) content.get(MESSAGE_METADATA_ATTRIBUTE));

            message.setResourceCreationDate(cin.getCreationTime());

        } catch (Exception e) {
            LOGGER.warn("Unexpected error while reading one of the topic {} data.", topicName, e);
        }
        return message;
    }

}
