/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
package org.eclipse.agail.protocol.om2m;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import embedded_libs.com.google.common.base.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.cfg4j.source.system.EnvironmentVariablesConfigurationSource;
import org.cfg4j.source.system.SystemPropertiesConfigurationSource;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.ProtocolManager;
import org.eclipse.agail.object.AbstractAgileObject;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.DeviceStatusType;
import org.eclipse.agail.object.StatusType;
import org.eclipse.agail.protocol.om2m.notification.Notification;
import org.eclipse.agail.protocol.om2m.notification.NotificationParsers;
import org.eclipse.agail.protocol.om2m.notification.NotificationResourceStatus;
import org.eclipse.agail.protocol.om2m.notification.NotificationVerificationRequest;
import org.eclipse.agail.protocol.om2m.utils.Request;
import org.eclipse.agail.protocol.om2m.utils.Response;
import org.eclipse.agail.protocol.om2m.utils.StringUtils;
import org.eclipse.om2m.commons.obix.Int;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.srcsolution.things.interworking.data.Message;
import com.srcsolution.things.interworking.data.Thing;
import com.srcsolution.things.interworking.data.Topic;
import com.srcsolution.things.interworking.mapper.*;
import com.srcsolution.things.interworking.mapper.content.ContentConverter;
import com.srcsolution.things.interworking.mapper.content.JsonContentConverter;
import com.srcsolution.things.interworking.mapper.content.ObixContentConverter;
import com.srcsolution.things.interworking.mapper.content.OperationContent;
import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceException;
import com.srcsolution.things.onem2m_client.ResourceFactory;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.http.HttpResourceFactory;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.ContentInstance;
import com.srcsolution.things.onem2m_client.resource.CseBase;

public class OneM2MProtocol extends AbstractAgileObject implements Protocol {

    private final Logger LOGGER = LoggerFactory.getLogger(OneM2MProtocol.class);

    /**
     * DBus bus name for the protocol manager
     */
    private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "org.eclipse.agail.ProtocolManager";

    /**
     * DBus bus path for the protocol manager
     */
    private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/org/eclipse/agail/ProtocolManager";

    private static final String AGILE_ONEM2M_PROTOCOL_BUS_NAME = "org.eclipse.agail.protocol.ONEM2M";

    private static final String AGILE_ONEM2M_PROTOCOL_BUS_PATH = "/org/eclipse/agail/protocol/ONEM2M";

    /**
     * DBus bus path for found new device signal
     */
    private static final String AGILE_NEW_DEVICE_SIGNAL_PATH = "/org/eclipse/agail/NewDevice";

    /**
     * DBus bus path for for new record/data reading
     */
    private static final String AGILE_NEW_RECORD_SIGNAL_PATH = "/org/eclipse/agail/NewRecord";

    /**
     * Protocol name
     */
    private static final String PROTOCOL_NAME = "oneM2M";

    private static final String RUNNING = "RUNNING";

    private static final String DRIVER_NAME = "ONEM2M";

    // Device status
    public static final String CONNECTED = "CONNECTED";

    public static final String AVAILABLE = "AVAILABLE";

    /**
     * Device list
     */
    protected List<DeviceOverview> deviceList = new ArrayList<DeviceOverview>();

    protected byte[] lastRecord;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private ScheduledFuture discoveryFuture;

    private ScheduledFuture subscriptionFuture;

    protected CseBase cseRoot;

    protected ApplicationEntity applicationEntityRoot;

    protected ContentConverter contentConverter;

    protected Om2mProperties properties;

    private SubscriptionService subscriptionService;

    public static Set<String> thingsConnected;

    public static Set<String> thingsSubscribed;

    public static Map<String, ResourceMapper> resourceMappersByApplicationId = null;

    public static Map<String, String> resourceMapperIdByDeviceAddress;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProtocolManager protocolManager;

    private ResourceFactory resourceFactory;

    private ZonedDateTime latestRecordDate = null;

    private static int counter = 0;

    public static void main(String[] args) {
        new OneM2MProtocol();
    }

    public OneM2MProtocol() {
        try {
            if (resourceMappersByApplicationId == null) {
                dbusConnect(AGILE_ONEM2M_PROTOCOL_BUS_NAME, AGILE_ONEM2M_PROTOCOL_BUS_PATH, this);
                resourceMappersByApplicationId = new HashMap<>();
                resourceMapperIdByDeviceAddress = new HashMap<>();
                this.properties = this.initProperties();
                this.subscriptionService = new SubscriptionService();
                this.resourceFactory = new HttpResourceFactory(getProperties().getRemoteNode());
                this.registerProtocol();
                this.initProtocol();
                this.initDevices();
                LOGGER.debug("{} devices found", this.deviceList.size());
                thingsConnected = new HashSet<>();
                thingsSubscribed = new HashSet<>();
            }
        } catch (Exception e) {
            LOGGER.error("Error whe instantiating om2m protocol", e);
        }
        LOGGER.debug("om2M protocol started!");
    }

    private Om2mProperties initProperties() {
        Om2mProperties properties;
        try {
            String packagePath = getClass().getPackage().getName().replaceAll("\\.", "/");
            properties = new Om2mProperties(
                    Om2mProperties.classpathSource(packagePath, "application"),
                    Om2mProperties.fileSource(System.getProperty("user.dir"), "application"),
                    Om2mProperties.fileSource(System.getProperty("user.dir"), "application-" + getName()),
                    Om2mProperties.fileSource(System.getProperty("user.dir"), "config", "application"),
                    Om2mProperties.fileSource(System.getProperty("user.dir"), "config", "application-" + getName()),
                    Om2mProperties.fileSource(System.getProperty("user.dir"), "configuration", "application"),
                    Om2mProperties.fileSource(System.getProperty("user.dir"), "configuration", "application-" + getName()),
                    new EnvironmentVariablesConfigurationSource(),
                    new SystemPropertiesConfigurationSource()
            );
        } catch (Exception e) {
            properties = new Om2mProperties();
        }
        return properties;
    }

    private void initProtocol() {
        if (StringUtils.isEmpty(getName())) {
            throw new IllegalArgumentException("Name must be not empty");
        }

        LOGGER.info("Init IPE {}", getClass().getName());
        LOGGER.debug("Loaded properties: name={}, resource.app_name={}", getName(), getProperties().getResourceAppName());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ee) {
            LOGGER.warn("Error while sleeping", ee);
        }
        // Get or create application root resource
        try {
            cseRoot = resourceFactory.retrieveRoot();

            cseRoot.find(ResourceType.APPLICATION_ENTITY, ThingResourcesConstants.APPLICATION_ENTITY_RESOURCE_LABEL)
                   .parallelStream()
                   .map(resource -> (ApplicationEntity) resource)
                   .forEach(appResource -> {
                       resourceMappersByApplicationId.put(appResource.getId(), new QueuedResourceMapper(appResource,
                                                                                                        contentConverter,
                                                                                                        getProperties().getToken(),
                                                                                                        getProperties().getMaxQueueSize()));
                   });

            counter = 0;
        } catch (Exception e) {
            counter++;
            if (counter <= 5) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ee) {
                    LOGGER.warn("Error while sleeping", ee);
                }
                LOGGER.warn("Request error: {}, trying again in main...", e.getMessage(), e);
                initProtocol();
            } else {
                throw new ResourceException("Request error: " + e.getMessage(), e, 0);
            }
        }

    }

    private void registerProtocol() {
        try {
            LOGGER.info("Protocol registration: getting protocol manager");
            protocolManager = (ProtocolManager) connection.getRemoteObject(AGILE_PROTOCOL_MANAGER_BUS_NAME, AGILE_PROTOCOL_MANAGER_BUS_PATH, ProtocolManager.class);
            LOGGER.info("Protocol registration: adding {}", AGILE_ONEM2M_PROTOCOL_BUS_NAME);
            protocolManager.Add(AGILE_ONEM2M_PROTOCOL_BUS_NAME);
            LOGGER.info("Protocol registration: {} added!", AGILE_ONEM2M_PROTOCOL_BUS_NAME);

        } catch (Exception e) {
            LOGGER.warn("Error while getting protocol manager or while registering protocol", e);
        }
    }

    private void initDevices() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ee) {
            LOGGER.warn("Error while sleeping", ee);
        }

        resourceMappersByApplicationId.forEach((id, resourceMapper) -> {
            Map<String, Thing> thingsDataMap = this.retrieveOm2mThings(resourceMapper.getReader());
            LOGGER.debug("{} things data found", thingsDataMap.size());
            thingsDataMap.forEach((key, thing) -> {
                resourceMapperIdByDeviceAddress.put(thing.getId(), id);
                DeviceOverview deviceOverview = new DeviceOverview(thing.getId(), AGILE_ONEM2M_PROTOCOL_BUS_NAME, thing.getConnectivity().getNetworkType(), CONNECTED);

                if (isNewDevice(deviceOverview)) {
                    deviceList.add(deviceOverview);
                    try {
                        ProtocolManager.FoundNewDeviceSignal foundNewDevSig = new ProtocolManager.FoundNewDeviceSignal(AGILE_NEW_DEVICE_SIGNAL_PATH, deviceOverview);
                        connection.sendSignal(foundNewDevSig);
                        LOGGER.debug("Creation signal sent!");
                    } catch (DBusException e) {
                        LOGGER.error("Error sending creation signal", e);
                    }
                }
            });
        });

    }

    private Map<String, Thing> retrieveOm2mThings(ResourceReader resourceReader) {
        Map<String, ThingResources> thingResourcesMap = resourceReader.mapThingResources();
        Map<String, Thing> thingsDataMap = new HashMap<>();

        // create each things if new
        thingResourcesMap.forEach((key, thingResources) -> {
            Thing thingData = resourceReader.readThing(thingResources);
            thingsDataMap.put(key, thingData);

        });

        thingResourcesMap.forEach((key, thingResources) -> {
            Thing thingData = thingsDataMap.get(key);
            readThingData(thingData, resourceReader, thingResources);

        });

        return thingsDataMap;
    }

    private void readThingData(Thing thingData, ResourceReader resourceReader, ThingResources thingResources) {
        try {
            thingData.setDevice(resourceReader.readDevice(thingResources));
        } catch (Exception ignored) {
        }
        try {
            thingData.setConnectivity(resourceReader.readConnectivity(thingResources));
        } catch (Exception ignored) {
        }
        Map<String, Topic> topicDataMap = resourceReader.readTopicsWithMessages(thingResources);
        thingData.setTopics(new ArrayList<>(topicDataMap.values()));
    }

    @Override
    public void Connect(String deviceAddress) throws DBusException {
        String resourceMapperApplicationId = resourceMapperIdByDeviceAddress.get(deviceAddress);
        ResourceMapper resourceMapper = resourceMappersByApplicationId.get(resourceMapperApplicationId);
        ThingResources thing = resourceMapper.getOrCreateThingResources(deviceAddress);

        if (thing == null) {
            LOGGER.debug("Device not connected {}", deviceAddress);
        } else {
            thingsConnected.add(deviceAddress);
            LOGGER.debug("Device connected {}", deviceAddress);
        }

    }

    @Override
    public void Disconnect(String deviceAddress) throws DBusException {
        thingsConnected.remove(deviceAddress);
        LOGGER.debug("Device disconnected {}", deviceAddress);
    }

    @Override
    public String DiscoveryStatus() throws DBusException {
        if (discoveryFuture != null) {
            if (discoveryFuture.isCancelled()) {
                return "NONE";
            } else {
                return RUNNING;
            }
        }
        return "NONE";
    }

    @Override
    public void StartDiscovery() throws DBusException {
        if (discoveryFuture != null) {
            LOGGER.info("Discovery already running");
            return;
        }

        LOGGER.info("Started discovery of om2M devices");
        Runnable task = () -> {
            LOGGER.debug("Checking for new devices");
            this.initProtocol();
            this.initDevices();
        };
        discoveryFuture = executor.scheduleWithFixedDelay(task, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void StopDiscovery() {
        if (discoveryFuture != null) {
            discoveryFuture.cancel(true);
            discoveryFuture = null;
        }
    }

    @Override
    public void Write(String deviceAddress, Map<String, String> profile, byte[] payload) throws DBusException {
        String resourceMapperApplicationId = resourceMapperIdByDeviceAddress.get(deviceAddress);
        ResourceMapper resourceMapper = resourceMappersByApplicationId.get(resourceMapperApplicationId);
        Thing thing = resourceMapper.read(deviceAddress);

        try {
            for (int i = 0; thing == null && i < 5; i++) {
                Thread.sleep(500);
                thing = resourceMapper.read(deviceAddress);
            }

            if (thing == null) {
                resourceMapper.getOrCreateThingResources(deviceAddress);
                Thread.sleep(500);
                thing = resourceMapper.read(deviceAddress);
            }

            String ipeName = profile.get("ipe");
            ipeName = ipeName == null || ipeName.isEmpty() ? "fake" : ipeName;

            JsonNode operationInstance = this.getOperationInstance(thing, ipeName);
            ContentConverter converter = this.getContentConverter(operationInstance);
            Map<String, Object> con = converter.deserialize(operationInstance.get("con").asText());
            OperationContent operation = (OperationContent) con.get(profile.get("operation").toUpperCase());
            LOGGER.debug("OP: {}", operation.getUrl());
            URIBuilder url = new URIBuilder("http://localhost:8181/~" + operation.getUrl());

            LOGGER.debug("Operation URL: {}", url.toString());
            url.addParameter("level", Integer.parseInt(new String(payload)) + "");
            HttpResponse operationResponse = org.apache.http.client.fluent.Request.Post(url.toString())
                                                                                  .addHeader("X-M2M-Origin", "admin:admin")
                                                                                  .addHeader("Content-Type", "application/json")
                                                                                  .execute().returnResponse();

            org.apache.http.HttpEntity httpEntity = operationResponse.getEntity();
            Integer status = operationResponse.getStatusLine().getStatusCode();
            String reason = operationResponse.getStatusLine().getReasonPhrase();
            String content = new Content(EntityUtils.toByteArray(httpEntity), ContentType.getOrDefault(httpEntity)).asString();

            LOGGER.debug("Operation: status {}, reason {} content {}", status, reason, content);

        } catch (Exception e) {
            LOGGER.error("Error while writing to {}, operation {}, payload {}", deviceAddress, profile.get("operation"), new String(payload), e);
        }
    }

    public ContentConverter getContentConverter(JsonNode operationInstance)
            throws ParseException, IOException {
        String cnf = operationInstance.get("cnf").asText().toLowerCase();
        ContentConverter converter;

        if (cnf.contains("obix")) {
            converter = new ObixContentConverter();
        } else if (cnf.contains("json")) {
            converter = new JsonContentConverter();
        } else {
            throw new ParseException("No converter for the cnf (" + cnf + ") of the operation. Only OBIX and JSON are currently supported.");
        }
        return converter;
    }

    public JsonNode getOperationInstance(Thing thing, String ipeName) throws IOException {
        //fixme this making of operation URL is risky
        String operationURL = "http://localhost:8181/~/middle-node/middle-node/" + ipeName + "/thing_" + thing.getId() + "/operations/la";

        HttpResponse response = org.apache.http.client.fluent.Request.Get(operationURL)
                                                                     .addHeader("X-M2M-Origin", "admin:admin")
                                                                     .addHeader("Content-Type", "application/json;ty=4")
                                                                     .execute().returnResponse();

        HttpEntity httpEntity = response.getEntity();
        Content content = new Content(EntityUtils.toByteArray(httpEntity), ContentType.getOrDefault(httpEntity));
        LOGGER.debug("Operation URL: {}", operationURL);
        return MAPPER.readTree(content.asString()).get("m2m:cin");
    }

    @Override
    public byte[] Read(String deviceAddress, Map<String, String> profile) throws DBusException {
        LOGGER.debug("Read deviceAddress: {}", deviceAddress);

        String resourceMapperApplicationId = resourceMapperIdByDeviceAddress.get(deviceAddress);
        LOGGER.debug("Read resourceMapperApplicationId: {}", resourceMapperApplicationId);
        ResourceMapper resourceMapper = resourceMappersByApplicationId.get(resourceMapperApplicationId);
        resourceMapper.getOrCreateThingResources(deviceAddress);

        Thing thing = resourceMapper.read(deviceAddress);
        byte[] record = null;
        if (thing != null) {
            LOGGER.debug("Thing id {}", thing.getId());

            record = this.readThingLastMessage(thing);
            lastRecord = record;
            LOGGER.info("Data read: {}", new String(record));

        } else {
            LOGGER.error("Device not found: {}", deviceAddress);
        }

        return record;
    }

    @Override
    public byte[] NotificationRead(String deviceAddress, Map<String, String> profile) throws DBusException {
        LOGGER.debug("NotificationRead deviceAddress: {}", deviceAddress);

        try {
            String resourceMapperApplicationId = this.resourceMapperIdByDeviceAddress.get(deviceAddress);
            LOGGER.debug("NotificationRead resourceMapperApplicationId: {}", resourceMapperApplicationId);

            ResourceMapper resourceMapper = this.resourceMappersByApplicationId.get(resourceMapperApplicationId);
            Thing thing = resourceMapper.read(deviceAddress);
            if (thing != null) {
                return this.readThingLastMessage(thing);
            } else {
                LOGGER.error("Device not found: {}", deviceAddress);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read data ", e);
            throw new DBusException("Failed to read data");
        }
        return null;
    }

    @Override
    public void Subscribe(String deviceAddress, Map<String, String> profile) throws DBusException {
        /*LOGGER.debug("Subscribing to {}....", deviceAddress);
        String resourceMapperApplicationId = this.resourceMapperIdByDeviceAddress.get(deviceAddress);
        ResourceMapper resourceMapper = this.resourceMappersByApplicationId.get(resourceMapperApplicationId);
        this.subscriptionService.subscribeToThingResource(deviceAddress, resourceMapper.getOrCreateThingResources(deviceAddress));
        this.thingsSubscribed.add(deviceAddress);
        LOGGER.debug("Subscribed to {}!", deviceAddress);*/
        if (subscriptionFuture == null) {
            Runnable task = () -> {

                try {
                    lastRecord = this.Read(deviceAddress, profile);
                    Protocol.NewRecordSignal newRecordSignal = new Protocol.NewRecordSignal(AGILE_NEW_RECORD_SIGNAL_PATH,
                                                                                            lastRecord, deviceAddress, profile);
                    LOGGER.debug("Notifying {}", this);
                    connection.sendSignal(newRecordSignal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            subscriptionFuture = executor.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void Unsubscribe(String deviceAddress, Map<String, String> profile) throws DBusException {
       /* if (this.thingsSubscribed.contains(deviceAddress)) {
            this.thingsSubscribed.remove(deviceAddress);
            LOGGER.debug("Unsubscribed to {}!", deviceAddress);
        }*/
        if (subscriptionFuture != null) {
            subscriptionFuture.cancel(true);
            subscriptionFuture = null;
            LOGGER.debug("Unsubscribed to {}!", deviceAddress);
        }
    }

    @Override
    public StatusType DeviceStatus(String deviceAddress) {
        StatusType deviceStatus = new StatusType(DeviceStatusType.CONNECTED.toString());

        if (!this.thingsConnected.contains(deviceAddress)) {
            deviceStatus = new StatusType(DeviceStatusType.DISCONNECTED.toString());
        }

        return deviceStatus;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String Status() {
        return RUNNING;
    }

    @Override
    public String Driver() {
        return DRIVER_NAME;
    }

    @Override
    public String Name() {
        return PROTOCOL_NAME;
    }

    @Override
    public byte[] Data() {
        return lastRecord;
    }

    @Override
    public List<DeviceOverview> Devices() {
        return deviceList;
    }

    private byte[] readThingLastMessage(Thing thing) {
        byte[] lastMessage = null;
        List<Topic> topics = thing.getTopics();

        for (Topic topic : topics) {
            for (Message message : topic.getMessages()) {

                if (latestRecordDate == null || latestRecordDate.isBefore(message.getTimestamp())) {
                    LOGGER.debug("Payload: {}, Time: {}, previous Latest: {}, latest", message.getPayload(), message.getTimestamp(), latestRecordDate, message.getTimestamp());
                    lastMessage = message.getPayload().getBytes();
                    latestRecordDate = message.getTimestamp();
                }
            }
        }

        return lastMessage;
    }

    private Notification readNotification(Request request) throws IOException {
        return NotificationParsers.parse(request.getBody(), request.getContentType());
    }

    public void processNotification(Request request, Response response, Map<String, String> profile) {

        this.handleNotification(request, notification -> {
            String thingId = request.getQueryParams().get("id").get(0);
            Resource resource = notification.getResource();

            if (resource instanceof ContentInstance) {
                try {
                    String resourceMapperApplicationId = this.resourceMapperIdByDeviceAddress.get(thingId);
                    ResourceMapper resourceMapper = this.resourceMappersByApplicationId.get(resourceMapperApplicationId);
                    Message message = resourceMapper.getReader().readMessage("payload", (ContentInstance) notification.getResource());

                    if (message != null && message.getPayload() != null) {
                        NewRecordSignal newRecordSignal = null;

                        lastRecord = message.getPayload().getBytes();
                        newRecordSignal = new NewRecordSignal(AGILE_NEW_RECORD_SIGNAL_PATH, lastRecord, thingId, profile);
                        LOGGER.debug("Notifying {}", this);
                        connection.sendSignal(newRecordSignal);
                    }
                } catch (DBusException e) {
                    LOGGER.error("Error when sending new data signal for {}", thingId);
                }
            }

        });

        response.setStatus(200);
    }

    private void handleNotification(Request request, Consumer<NotificationResourceStatus> consumer) {
        try {
            Notification notification = readNotification(request);
            if (notification instanceof NotificationResourceStatus) {
                LOGGER.trace("Receive notification {}", notification);
                consumer.accept((NotificationResourceStatus) notification);

            } else if (notification instanceof NotificationVerificationRequest) {
                LOGGER.trace("Receive notification verification (}", notification);
                // always ok
            }

        } catch (Exception e) {
            LOGGER.debug("Receive bad notification", e);
        }
    }

    public Om2mProperties getProperties() {
        return properties;
    }

    public void setProperties(Om2mProperties properties) {
        this.properties = properties;
    }

    protected final String getApplicationEntityName() {
        String applicationName = getProperties().getResourceAppName();
        if (applicationName == null || applicationName.isEmpty()) {
            applicationName = getName();
        }
        return applicationName;
    }

    protected final String getPointOfAccess() {
        String pointOfAccess = getProperties().getResourcePointOfAccess();

        if (Strings.isNullOrEmpty(pointOfAccess)) {
            pointOfAccess = getName();
        }

        return pointOfAccess;
    }

    public String getName() {
        return AGILE_ONEM2M_PROTOCOL_BUS_NAME;
    }

    /**
     * Check if the device is newly discovered device
     */
    private boolean isNewDevice(DeviceOverview device) {
        for (DeviceOverview dev : deviceList) {
            if (dev.getId().equals(device.getId())) {
                return false;
            }
        }
        return true;
    }
}
