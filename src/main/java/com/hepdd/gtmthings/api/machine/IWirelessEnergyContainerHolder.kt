package com.hepdd.gtmthings.api.machine

import com.hepdd.gtmthings.api.capability.IBindable
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer.Companion.getOrCreateContainer

interface IWirelessEnergyContainerHolder: IBindable {

    fun getWirelessEnergyContainerCache(): WirelessEnergyContainer?

    fun setWirelessEnergyContainerCache(container: WirelessEnergyContainer)

    fun getWirelessEnergyContainer(): WirelessEnergyContainer? {
        if (getWirelessEnergyContainerCache() == null) {
            val container = getOrCreateContainer(getUUID())
            setWirelessEnergyContainerCache(container)
        }
        return getWirelessEnergyContainer()
    }
}