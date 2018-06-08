package org.eclipse.agail.protocol.om2m.notification;

import java.io.InputStream;
import java.time.ZonedDateTime;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.google.common.base.Splitter;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ShortName;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.resource.AbstractResource;

import static org.slf4j.LoggerFactory.getLogger;

public class NotificationParserXml extends NotificationParser {

    public static final Logger LOGGER = getLogger(NotificationParserXml.class);

    @Override
    boolean support(String mediaType) {
        return MimeMediaType.XML.equalsIgnoreCase(mediaType);
    }

    public static final XMLInputFactory factory = XMLInputFactory.newFactory();

    @Override
    public Notification parse(InputStream stream) {
        XMLStreamReader sr = null;
        try {
            sr = factory.createXMLStreamReader(stream);

            sr.next(); // to point to root <m2m:sgn>
            if (!ShortName.NOTIFICATION.equals(sr.getLocalName())) {
                throw new InvalidNotificationException("Not a valid xml notification, root tag invalid");
            }
            NotificationResourceStatus notification = new NotificationResourceStatus();

            int eventType;
            while (sr.hasNext()) {
                eventType = sr.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String elementName = sr.getLocalName();
                        switch (elementName) {
                            case ShortName.NOTIFICATION_EVENT:
                                readNotificationEvent(notification, sr);
                                break;
                            case ShortName.SUBSCRIPTION_DELETION:
                                break;
                            case ShortName.SUBSCRIPTION_REFERENCE:
                                sr.next();
                                notification.setSubscriptionReference(this.getElementText(sr));
                                break;
                            case ShortName.VERIFICATION_REQUEST:
                                return new NotificationVerificationRequest();
                        }
                        break;

                    case XMLEvent.END_ELEMENT:
                        break;

                    case XMLEvent.END_DOCUMENT:
                        break;
                }
            }

            return notification;

        } catch (Exception e) {
            LOGGER.warn("Cannot parse notification", e);
            throw new InvalidNotificationException("Not a valid xml notification", e);
        } finally {
            if (sr != null) {
                try {
                    sr.close();
                } catch (XMLStreamException ignored) {
                }
            }
        }
    }

    private void readNotificationEvent(NotificationResourceStatus notification, XMLStreamReader sr) throws XMLStreamException {
        while (sr.hasNext()) {
            int eventType = sr.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elementName = sr.getLocalName();
                    switch (elementName) {
                        case ShortName.REPRESENTATION:
                            readRepresentation(notification, sr);
                            break;
                        case ShortName.RESOURCE_STATUS:
                            sr.next();
                            ResourceStatus status = ResourceStatus.valueOf(Integer.valueOf(this.getElementText(sr)));
                            notification.setStatus(status);
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ShortName.NOTIFICATION_EVENT.equals(sr.getLocalName())) {
                        return;
                    }
                    break;
            }
        }
        throw new XMLStreamException("Premature end of file");
    }

    private void readRepresentation(NotificationResourceStatus notification, XMLStreamReader sr) throws XMLStreamException {
        ResourceAttributes resource = new ResourceAttributes();
        for (int i = 0; i < sr.getAttributeCount(); i++) {
            if (ShortName.RESOURCE_NAME.equals(sr.getAttributeLocalName(i))) {
                resource.setName(sr.getAttributeValue(i));
            }
        }

        while (sr.hasNext()) {
            int eventType = sr.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elementName = sr.getLocalName();
                    sr.next();
                    switch (elementName) {
                        case ShortName.RESOURCE_ID:
                            resource.setId(this.getElementText(sr));
                            break;
                        case ShortName.RESOURCE_TYPE:
                            resource.setType(Integer.valueOf(this.getElementText(sr)));
                            break;
                        case ShortName.PARENT_ID:
                            resource.setParentId(this.getElementText(sr));
                            break;
                        case ShortName.CREATION_TIME:
                            ZonedDateTime ct = ZonedDateTime.parse(this.getElementText(sr), AbstractResource.DATE_TIME_FORMATTER);
                            resource.setCreationTime(ct);
                            break;
                        case ShortName.LAST_MODIFIED_TIME:
                            ZonedDateTime lt = ZonedDateTime.parse(this.getElementText(sr), AbstractResource.DATE_TIME_FORMATTER);
                            resource.setLastModifiedTime(lt);
                            break;
                        case ShortName.LABELS:
                            resource.setLabels(Splitter.on(' ').splitToList(this.getElementText(sr)));
                            break;
                        default:
                            resource.setAttribute(elementName, this.getElementText(sr));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ShortName.REPRESENTATION.equals(sr.getLocalName())) {
                        notification.setResource(resource);
                        return;
                    }
                    break;
            }
        }
        throw new XMLStreamException("Premature end of file");
    }

    private String getElementText(XMLStreamReader sr) throws XMLStreamException {
        String text = "";

        while (sr.getEventType() == XMLStreamConstants.CHARACTERS) {
            text += sr.getText();
            sr.next();
        }
        return text;
    }

}
