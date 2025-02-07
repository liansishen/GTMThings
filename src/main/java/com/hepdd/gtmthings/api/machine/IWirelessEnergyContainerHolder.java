package com.hepdd.gtmthings.api.machine;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;

import java.util.UUID;

public interface IWirelessEnergyContainerHolder {

    UUID getUUID();

    void setWirelessEnergyContainerCache(WirelessEnergyContainer container);

    WirelessEnergyContainer getWirelessEnergyContainerCache();

    default WirelessEnergyContainer getWirelessEnergyContainer() {
        if (getWirelessEnergyContainerCache() == null) {
            WirelessEnergyContainer container = WirelessEnergyContainer.getOrCreateContainer(getUUID());
            setWirelessEnergyContainerCache(container);
        }
        return getWirelessEnergyContainerCache();
    }
}
