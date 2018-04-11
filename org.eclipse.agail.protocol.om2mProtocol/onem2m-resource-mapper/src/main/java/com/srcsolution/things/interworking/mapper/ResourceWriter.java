package com.srcsolution.things.interworking.mapper;

import java.util.*;

import org.slf4j.Logger;

import com.srcsolution.things.interworking.data.*;
import com.srcsolution.things.interworking.mapper.content.ContentConverter;
import com.srcsolution.things.interworking.mapper.content.OperationContent;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.Container;

import static com.srcsolution.things.interworking.mapper.ThingResourcesConstants.*;
import static org.slf4j.LoggerFactory.getLogger;

public class ResourceWriter {

    public static final Logger LOGGER = getLogger(ResourceWriter.class);

    private final ApplicationEntity rootAe;

    private final ContentConverter converter;

    private final UUID token;

    public ResourceWriter(ApplicationEntity rootAe, ContentConverter converter, UUID token) {
        this.rootAe = rootAe;
        this.converter = converter;
        this.token = token;
    }

    public void write(Thing thing, ThingResources thingResources, Thing last) {
        if (thing == null) {
            LOGGER.warn("Cannot write resources for null thing");
            return;
        }
        if (thingResources == null) {
            LOGGER.warn("Thing resources must be not null");
            return;
        }

        LOGGER.trace("Write resources for thing {} in {}", thing.getId(), rootAe);
        writeTopicsAndMessages(thing, thingResources, last);

        writeOperations(thing, thingResources, last);

        writeCustom(thing, thingResources, last);
        writeConnectivity(thing, thingResources, last);
        writeDevice(thing, thingResources, last);
    }

    private void writeTopicsAndMessages(Thing thing, ThingResources thingResources, Thing last) {
        List<Topic> topics = thing.getTopics();
        Container messagesContainer = thingResources.getMessagesContainer();

        for (Topic topic : topics) {
            String topicName = topic.getName();
            try {
                Container topicContainer = thingResources.getTopicContainer(topicName);
                if (topicContainer == null) {
                    topicContainer = ThingResources.getOrCreateChildContainer(messagesContainer, topicName, topic.getMaxInstances());
                    thingResources.setTopicContainer(topicName, topicContainer);
                }

                writeMessages(topic.getMessages(), topicContainer);
            } catch (Exception e) {
                LOGGER.warn("Cannot write messages for topic {}", topicName);
            }
        }
    }

    private void writeMessages(List<Message> messages, Container topicResource) {
        for (Message message : messages) {
            Map<String, Object> content = new HashMap<>();
            content.put(MESSAGE_PAYLOAD_ATTRIBUTE, message.getPayload());
            content.put(MESSAGE_TIMESTAMP_ATTRIBUTE, message.getTimestamp());
            if (message.getLatitude() != null) {
                content.put(MESSAGE_LATITUDE_ATTRIBUTE, message.getLatitude());
            }
            if (message.getLongitude() != null) {
                content.put(MESSAGE_LONGITUDE_ATTRIBUTE, message.getLongitude());
            }
            if (message.getMetadata() != null && !message.getMetadata().isEmpty()) {
                content.put(MESSAGE_METADATA_ATTRIBUTE, message.getMetadata());
            }

            buildContentInstance(topicResource, content);
            LOGGER.trace("Messages data written into {}", topicResource);
        }
    }

    private void writeCustom(Thing thing, ThingResources thingResources, Thing last) {
        if (thing.hasPropertiesChanged(last)) {
            Container customResource = thingResources.getThingPropsContainer();
            Map<String, Object> content = new HashMap<>();
            content.put(THING_ID_ATTRIBUTE, thing.getId());
            String name = thing.getName();
            if (name == null || name.isEmpty()) {
                name = thing.getId();
            }
            content.put(THING_NAME_ATTRIBUTE, name);
            content.putAll(thing.getAdditionalProperties());

            buildContentInstance(customResource, content);
            LOGGER.trace("Device data written in {}", customResource);
        }
    }

    private void writeConnectivity(Thing thing, ThingResources thingResources, Thing last) {
        LOGGER.trace("Start writing connectivity data");
        Connectivity connectivity = thing.getConnectivity();
        if (connectivity != null) {
            if (last == null || connectivity.hasChanged(last.getConnectivity())) {
                Container connectivityResource = thingResources.getConnectivityContainer();

                Map<String, Object> content = new HashMap<>();
                content.put(CONNECTIVITY_ID_ATTRIBUTE, connectivity.getId());
                content.put(CONNECTIVITY_STATUS_ATTRIBUTE, connectivity.getStatus());
                content.put(CONNECTIVITY_RAW_STATUS_ATTRIBUTE, connectivity.getRawStatus());
                content.put(CONNECTIVITY_NETWORK_TYPE_ATTRIBUTE, connectivity.getNetworkType());
                content.putAll(connectivity.getAdditionalProperties());

                buildContentInstance(connectivityResource, content);
                LOGGER.trace("Connectivity data written in {}", connectivityResource);
            }
        }
    }

    private void writeDevice(Thing thing, ThingResources thingResources, Thing last) {
        Device device = thing.getDevice();
        if (device != null) {
            if (last == null || device.hasChanged(last.getDevice())) {
                Container deviceResource = thingResources.getDeviceContainer();
                Map<String, Object> content = new HashMap<>();
                content.put(DEVICE_ID_ATTRIBUTE, device.getId());
                content.put(DEVICE_NAME_ATTRIBUTE, device.getName());
                content.put(DEVICE_PRODUCT_NAME_ATTRIBUTE, device.getProductName());
                content.put(DEVICE_MANUFACTURER_NAME_ATTRIBUTE, device.getManufacturerName());
                content.put(DEVICE_SERIAL_NUMBER_ATTRIBUTE, device.getSerialNumber());
                content.put(DEVICE_BATTERY_STATUS_ATTRIBUTE, device.getBatteryStatus());
                content.put(DEVICE_BATTERY_LEVEL_ATTRIBUTE, device.getBatteryLevel());
                content.put(DEVICE_REFERENCE_ATTRIBUTE, device.getReference());
                content.putAll(device.getAdditionalProperties());

                buildContentInstance(deviceResource, content);
                LOGGER.trace("Device data written in {}", deviceResource);
            }
        }
    }

    private void writeOperations(Thing thing, ThingResources thingResources, Thing last) {
        Set<OperationData> operations = thing.getOperations();
        if (!operations.isEmpty() && thing.hasOperationsChanged(last)) {
            Container operationsResource = thingResources.getOperationsContainer();
            Map<String, Object> content = new HashMap<>();
            for (OperationData operation : operations) {
                content.put(operation.getName(), new OperationContent(rootAe, operation, token));
            }
            buildContentInstance(operationsResource, content);
            LOGGER.trace("Operations data written into {}", operationsResource);
        }
    }

    private void buildContentInstance(Container container, Map<String, Object> content) {
        container.buildContentInstance()
                 .setContentInfo(converter.getMediaType())
                 .setContent(converter.serialize(content))
                 .create();
    }

}
