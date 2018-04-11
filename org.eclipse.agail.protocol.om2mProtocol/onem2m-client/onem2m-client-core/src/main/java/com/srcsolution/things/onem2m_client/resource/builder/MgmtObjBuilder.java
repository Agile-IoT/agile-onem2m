package com.srcsolution.things.onem2m_client.resource.builder;

import com.srcsolution.things.onem2m_client.ResourceBuilder;
import com.srcsolution.things.onem2m_client.resource.MgmtObj;

public abstract class MgmtObjBuilder {

    public static abstract class DeviceInfo extends ResourceBuilder<MgmtObj.DeviceInfo> {

        protected String deviceLabel;

        protected String manufacturer;

        protected String model;

        protected String deviceType;

        protected String firmwareVersion;

        protected String softwareVersion;

        protected String hardwareVersion;

        public DeviceInfo setDeviceLabel(String deviceLabel) {
            this.deviceLabel = deviceLabel;
            return this;
        }

        public DeviceInfo setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public DeviceInfo setModel(String model) {
            this.model = model;
            return this;
        }

        public DeviceInfo setDeviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public DeviceInfo setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public DeviceInfo setSoftwareVersion(String softwareVersion) {
            this.softwareVersion = softwareVersion;
            return this;
        }

        public DeviceInfo setHardwareVersion(String hardwareVersion) {
            this.hardwareVersion = hardwareVersion;
            return this;
        }
    }

    public abstract class Memory extends ResourceBuilder<MgmtObj.Memory> {

        protected Integer memoryAvailable;

        protected Integer memoryTotal;

        public Memory setMemoryAvailable(Integer memoryAvailable) {
            this.memoryAvailable = memoryAvailable;
            return this;
        }

        public Memory setMemoryTotal(Integer memoryTotal) {
            this.memoryTotal = memoryTotal;
            return this;
        }
    }

    public abstract class Battery extends ResourceBuilder<MgmtObj.Battery> {

        protected Long batteryLevel;

        protected String batteryStatus;

        public Battery setBatteryLevel(Long batteryLevel) {
            this.batteryLevel = batteryLevel;
            return this;
        }

        public Battery setBatteryStatus(String batteryStatus) {
            this.batteryStatus = batteryStatus;
            return this;
        }
    }

}
