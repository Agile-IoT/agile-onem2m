package org.eclipse.agail.protocol.om2m;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;

import com.srcsolution.things.interworking.mapper.ThingResources;
import com.srcsolution.things.onem2m_client.Resource;

import static org.slf4j.LoggerFactory.getLogger;

public class SubscriptionService {

    public static final Logger LOGGER = getLogger(SubscriptionService.class);

    public static final String RESOURCE_NOTIFICATIONS_PATH = "/resource_notifications";

    private String callbackUrlPrefix = "http://localhost:8083";

    private String coapCallbackUrlPrefix = "coap://localhost:5083";

    public SubscriptionService() {
    }

    /**
     * Build callback uri based on remote resources node binding and related domain entity.
     *
     * @param segment entity type
     * @param id      entity id
     * @param topic   sub topic if entity is a thing (optional)
     */
    private String buildCallbackUrl(String segment, UUID id, String topic) {
        return buildHttpCallbackUrl(segment, id, topic);
    }

    private String buildCallbackUrl(String segment, UUID id) {
        return buildCallbackUrl(segment, id, null);
    }

    private String buildCoapCallbackUrl(String segment, Object id, String topic) {

        return coapCallbackUrlPrefix + RESOURCE_NOTIFICATIONS_PATH + "?rt=" + segment + "&id=" + id;
    }

    private String buildHttpCallbackUrl(String segment, Object id, String topic) {
        return callbackUrlPrefix + RESOURCE_NOTIFICATIONS_PATH + "?id=" + String.valueOf(id) + "&segment=" + segment + "&topic=" + topic;
    }

    public String makeSubscriptionResourceName(Resource resource) {
        return "sub_pth_" + resource.getName();
    }

    public Optional<Resource> getSubscriptionResource(Resource resource) {
        return resource.child(makeSubscriptionResourceName(resource));
    }

    // MESSAGES SUBSCRIPTION / NOTIFICATION

    public static final String THINGS_SEGMENT = "th";

    public void subscribeToThingResource(String thingId, ThingResources resources) {
        LOGGER.debug("Subscribe to thing {} ...", thingId);
        try {
            resources.getTopicContainers().forEach((topicName, container) -> {
                try {
                    container.subscribe(
                            makeSubscriptionResourceName(container),
                            buildCallbackUrl(THINGS_SEGMENT, UUID.fromString(thingId), topicName));
                } catch (Exception e) {
                    LOGGER.warn("Cannot subscribe to messages of thing {} on topic {}", thingId, topicName, e);
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Cannot subscribe to topics and messages of thing {}", thingId, e);
        }
    }

}
