package org.eclipse.agail.protocol.om2m.notification;

import java.io.InputStream;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ShortName;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.resource.AbstractResource;

import static org.slf4j.LoggerFactory.getLogger;

public class NotificationParserJson extends NotificationParser {

    public static final Logger LOGGER = getLogger(NotificationParserJson.class);

    @Override
    boolean support(String mediaType) {
        return MimeMediaType.JSON.equalsIgnoreCase(mediaType);
    }

    public static final ObjectMapper mapper = new ObjectMapper();

    @Override
    Notification parse(InputStream stream) {
        try {
            JsonNode root = mapper.readTree(stream);

            JsonNode sgn = root.get("m2m:sgn");
            if (sgn != null) {
                if (sgn.has(ShortName.VERIFICATION_REQUEST)) {
                    return new NotificationVerificationRequest();

                } else {
                    NotificationResourceStatus notification = new NotificationResourceStatus();
                    notification.setSubscriptionReference(sgn.path(ShortName.SUBSCRIPTION_REFERENCE).asText());

                    JsonNode nev = sgn.path(ShortName.NOTIFICATION_EVENT);
                    ResourceStatus status = ResourceStatus.valueOf(nev.path(ShortName.RESOURCE_STATUS).asInt());
                    notification.setStatus(status);

                    JsonNode rep = nev.path(ShortName.REPRESENTATION);
                    ResourceAttributes resource = new ResourceAttributes();
                    rep.fieldNames().forEachRemaining(field -> {
                        JsonNode value = rep.path(field);
                        switch (field) {
                            case ShortName.RESOURCE_ID:
                                resource.setId(value.asText());
                                break;
                            case ShortName.RESOURCE_TYPE:
                                resource.setType(value.asInt());
                                break;
                            case ShortName.RESOURCE_NAME:
                                resource.setName(value.asText());
                                break;
                            case ShortName.PARENT_ID:
                                resource.setParentId(value.asText());
                                break;
                            case ShortName.CREATION_TIME:
                                ZonedDateTime ct = ZonedDateTime.parse(value.asText(), AbstractResource.DATE_TIME_FORMATTER);
                                resource.setCreationTime(ct);
                                break;
                            case ShortName.LAST_MODIFIED_TIME:
                                ZonedDateTime lt = ZonedDateTime.parse(value.asText(), AbstractResource.DATE_TIME_FORMATTER);
                                resource.setLastModifiedTime(lt);
                                break;
                            case ShortName.LABELS:
                                resource.setLabels(Splitter.on(' ').splitToList(value.asText()));
                                break;
                            default:
                                resource.setAttribute(field, value.asText());
                                break;
                        }
                    });
                    notification.setResource(resource);

                    return notification;
                }
            }

            throw new InvalidNotificationException("Not a valid xml notification");

        } catch (Exception e) {
            LOGGER.warn("Cannot parse notification", e);
            throw new InvalidNotificationException("Not a valid xml notification", e);
        }
    }

}
