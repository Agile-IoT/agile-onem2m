package com.srcsolution.things.onem2m_client.resource;

import java.net.URL;

import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;

public interface MgmtObj extends Resource {

    default ResourceType getType() {
        return ResourceType.MANAGEMENT_OBJECT;
    }

    MgmtObjType getMgmtDefinition();

    interface Software extends MgmtObj {}

    interface Battery extends MgmtObj {

        Long getLevel();

        Integer getStatus();
    }

    interface DeviceCapability extends MgmtObj {}

    interface AreaNetworkInfo extends MgmtObj {}

    interface AreaNetworkDeviceInfo extends MgmtObj {}

    interface Reboot extends MgmtObj {}

    interface Firmware extends MgmtObj {

        String getVersion();

        String getFirmwareName();

        URL getURL();

        Boolean getUpdate();

        Integer getUpdateStatus();
    }

    interface Memory extends MgmtObj {

        Integer getMemoryAvailable();

        Integer getMemoryTotal();

    }

    interface DeviceInfo extends MgmtObj {

        String getDeviceLabel();

        String getManufacturer();

        String getModel();

        String getDeviceType();

        String getFirmwareVersion();

        String getSoftwareVersion();

        String getHardwareVersion();

    }

}
