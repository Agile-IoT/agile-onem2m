package com.srcsolution.things.interworking.data;

import java.util.HashMap;
import java.util.Objects;

import com.srcsolution.things.onem2m_client.resource.Container;

public class Device {

    private String id;

    private String name;

    private Container om2mResource;

    private HashMap<String, Object> additionalProperties;

    private String manufacturerName;

    private String reference;

    private String serialNumber;

    private DeviceStatus status;

    private String model;

    private String deviceType;

    private Integer batteryLevel;

    private DeviceBatteryStatus batteryStatus = DeviceBatteryStatus.UNKNOWN;

    private Integer memoryFree;

    private Integer memoryTotal;

    private String productName;

    public Device() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        return additionalProperties;
    }

    public void setAdditionalProperties(HashMap<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void addProperty(String name, String value) {
        getAdditionalProperties().put(name, value);
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Device setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public Device setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
        return this;
    }

    public String getModel() {
        return model;
    }

    public Device setModel(String model) {
        this.model = model;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Device setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public Device setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
        return this;
    }

    public void setBatteryStatus(DeviceBatteryStatus batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public DeviceBatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public Integer getMemoryFree() {
        return memoryFree;
    }

    public Device setMemoryFree(Integer memoryFree) {
        this.memoryFree = memoryFree;
        return this;
    }

    public Integer getMemoryTotal() {
        return memoryTotal;
    }

    public Device setMemoryTotal(Integer memoryTotal) {
        this.memoryTotal = memoryTotal;
        return this;
    }

    public Container getOm2mResource() {
        return om2mResource;
    }

    public void setOm2mResource(Container om2mResource) {
        this.om2mResource = om2mResource;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean hasChanged(Device other) {
        if (other == null) return true;

        int currentHash = Objects.hash(this.id, this.name, this.additionalProperties, this.manufacturerName, this.serialNumber,
                                       this.status, this.model, this.deviceType, this.batteryLevel, this.batteryStatus,
                                       this.memoryFree, this.memoryTotal, this.productName, this.reference);
        int newHash = Objects.hash(other.id, other.name, other.additionalProperties, other.manufacturerName, other.serialNumber,
                                   other.status, other.model, other.deviceType, other.batteryLevel, other.batteryStatus,
                                   other.memoryFree, other.memoryTotal, other.productName, other.reference);

        return currentHash != newHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(getId(), device.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
