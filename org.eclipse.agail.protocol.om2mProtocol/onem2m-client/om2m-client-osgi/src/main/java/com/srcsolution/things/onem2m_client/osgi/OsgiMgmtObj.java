package com.srcsolution.things.onem2m_client.osgi;

import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.MgmtResource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

import com.srcsolution.things.onem2m_client.resource.MgmtObj;
import com.srcsolution.things.onem2m_client.resource.MgmtObjType;
import com.srcsolution.things.onem2m_client.resource.builder.MgmtObjBuilder;

public class OsgiMgmtObj extends OsgiResource implements MgmtObj {

    private final MgmtResource base;

    public OsgiMgmtObj(RequestSender requestSender) {
        this(requestSender, null);
    }

    public OsgiMgmtObj(RequestSender requestSender, String resourceId) {
        super(requestSender, resourceId);
        base = (MgmtResource) super.om2mResource;
    }

    @Override
    public MgmtObjType getMgmtDefinition() {
        return MgmtObjType.valueOf(base.getMgmtDefinition().intValue());
    }

    public static class OsgiDeviceInfo extends OsgiMgmtObj implements MgmtObj.DeviceInfo {

        private final org.eclipse.om2m.commons.resource.DeviceInfo base;

        public OsgiDeviceInfo(RequestSender requestSender) {
            this(requestSender, null);
        }

        public OsgiDeviceInfo(RequestSender requestSender, String resourceId) {
            super(requestSender, resourceId);
            base = (org.eclipse.om2m.commons.resource.DeviceInfo) om2mResource;
        }

        @Override
        public String getDeviceLabel() {
            return base.getDeviceLabel();
        }

        @Override
        public String getManufacturer() {
            return base.getManufacturer();
        }

        @Override
        public String getModel() {
            return base.getModel();
        }

        @Override
        public String getDeviceType() {
            return base.getDeviceType();
        }

        @Override
        public String getFirmwareVersion() {
            return base.getFwVersion();
        }

        @Override
        public String getSoftwareVersion() {
            return base.getSwVersion();
        }

        @Override
        public String getHardwareVersion() {
            return base.getHwVersion();
        }

        public static class Builder extends MgmtObjBuilder.DeviceInfo {

            private final OsgiResource parent;

            public Builder(OsgiResource parent) {
                this.parent = parent;
            }

            @Override
            public DeviceInfo create(String name) {
                org.eclipse.om2m.commons.resource.DeviceInfo instance = new org.eclipse.om2m.commons.resource.DeviceInfo();
                instance.setName(name);
                if (this.labels != null) {
                    instance.getLabels().addAll(this.labels);
                }
                instance.setMgmtDefinition(BigInteger.valueOf(MgmtObjType.DEVICE_INFO.getValue()));
                instance.setDeviceLabel(this.deviceLabel);
                instance.setManufacturer(this.manufacturer);
                instance.setModel(this.model);
                instance.setDeviceType(this.deviceType);
                instance.setFwVersion(this.firmwareVersion);
                instance.setSwVersion(this.softwareVersion);
                instance.setHwVersion(this.hardwareVersion);

                ResponsePrimitive resp = parent.requestSender.createResource(parent.getId(), name, instance, ResourceType.MGMT_OBJ);

                org.eclipse.om2m.commons.resource.DeviceInfo content = (org.eclipse.om2m.commons.resource.DeviceInfo) resp.getContent();
                return new OsgiDeviceInfo(parent.requestSender, content.getResourceID());
            }

        }
    }

    public class OsgiMemory extends OsgiMgmtObj implements MgmtObj.Memory {

        private final org.eclipse.om2m.commons.resource.Memory base;

        public OsgiMemory(RequestSender requestSender) {
            this(requestSender, null);
        }

        public OsgiMemory(RequestSender requestSender, String resourceId) {
            super(requestSender, resourceId);
            base = (org.eclipse.om2m.commons.resource.Memory) om2mResource;
        }

        @Override
        public Integer getMemoryAvailable() {
            return base.getMemAvailable().intValue();
        }

        @Override
        public Integer getMemoryTotal() {
            return base.getMemTotal().intValue();
        }
    }

    public class OsgiBattery extends OsgiMgmtObj implements MgmtObj.Battery {

        private final org.eclipse.om2m.commons.resource.Battery base;

        public OsgiBattery(RequestSender requestSender) {
            this(requestSender, null);
        }

        public OsgiBattery(RequestSender requestSender, String resourceId) {
            super(requestSender, resourceId);
            base = (org.eclipse.om2m.commons.resource.Battery) om2mResource;
        }

        @Override
        public Long getLevel() {
            return base.getBatteryLevel();
        }

        @Override
        public Integer getStatus() {
            return base.getBatteryStatus().intValue();
        }
    }

}
