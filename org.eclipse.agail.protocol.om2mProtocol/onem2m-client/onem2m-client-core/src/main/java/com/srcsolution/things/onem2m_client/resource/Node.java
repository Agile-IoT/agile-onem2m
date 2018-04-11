package com.srcsolution.things.onem2m_client.resource;

import java.util.Optional;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;

public interface Node extends Resource {

    default ResourceType getType() {
        return ResourceType.NODE;
    }

    String getNodeId();

    default Optional<MgmtObj> getMgmtObj(MgmtObjType type) {
        return children(ResourceType.MANAGEMENT_OBJECT).stream()
                                                       .map(child -> (MgmtObj) child)
                                                       .filter(mgmtObj -> mgmtObj.getMgmtDefinition().equals(type))
                                                       .findFirst();
    }

    default Optional<MgmtObj.Software> getSoftwareMgmtObj() {
        return getMgmtObj(MgmtObjType.SOFTWARE).map(mgmtObj -> (MgmtObj.Software) mgmtObj);
    }

    default Optional<MgmtObj.Firmware> getFirmwareMgmtObj() {
        return getMgmtObj(MgmtObjType.FIRMWARE).map(mgmtObj -> (MgmtObj.Firmware) mgmtObj);
    }

    default Optional<MgmtObj.Battery> getBatteryMgmtObj() {
        return getMgmtObj(MgmtObjType.BATTERY).map(mgmtObj -> (MgmtObj.Battery) mgmtObj);
    }

    default Optional<MgmtObj.DeviceInfo> getDeviceInfoMgmtObj() {
        return getMgmtObj(MgmtObjType.DEVICE_INFO).map(mgmtObj -> (MgmtObj.DeviceInfo) mgmtObj);
    }

    default Optional<MgmtObj.DeviceCapability> getDeviceCapabilityMgmtObj() {
        return getMgmtObj(MgmtObjType.DEVICE_CAPABILITY).map(mgmtObj -> (MgmtObj.DeviceCapability) mgmtObj);
    }

    default Optional<MgmtObj.Memory> getMemoryMgmtObj() {
        return getMgmtObj(MgmtObjType.MEMORY).map(mgmtObj -> (MgmtObj.Memory) mgmtObj);
    }

    default Optional<MgmtObj.Reboot> getRebootMgmtObj() {
        return getMgmtObj(MgmtObjType.REBOOT).map(mgmtObj -> (MgmtObj.Reboot) mgmtObj);
    }

    default Optional<MgmtObj.AreaNetworkInfo> getAreaNetworkInfoMgmtObj() {
        return getMgmtObj(MgmtObjType.AREA_NETWORK_INFO).map(mgmtObj -> (MgmtObj.AreaNetworkInfo) mgmtObj);
    }

    default Optional<MgmtObj.AreaNetworkDeviceInfo> getAreaNetworkDeviceInfoMgmtObj() {
        return getMgmtObj(MgmtObjType.AREA_NETWORK_DEVICE_INFO).map(mgmtObj -> (MgmtObj.AreaNetworkDeviceInfo) mgmtObj);
    }

}
