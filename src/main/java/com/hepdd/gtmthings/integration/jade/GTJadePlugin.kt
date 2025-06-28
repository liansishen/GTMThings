package com.hepdd.gtmthings.integration.jade

import com.gregtechceu.gtceu.api.block.MetaMachineBlock
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.hepdd.gtmthings.integration.jade.provider.WirelessEnergyHatchProvider
import com.hepdd.gtmthings.integration.jade.provider.WirelessOpticalComputationHatchProvider
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin

@WailaPlugin
class GTJadePlugin:IWailaPlugin {

    override fun register(registration: IWailaCommonRegistration) {
        registration.registerBlockDataProvider(WirelessEnergyHatchProvider(), MetaMachineBlockEntity::class.java)
        registration.registerBlockDataProvider(
            WirelessOpticalComputationHatchProvider(),
            MetaMachineBlockEntity::class.java
        )
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(WirelessEnergyHatchProvider(), MetaMachineBlock::class.java)
        registration.registerBlockComponent(WirelessOpticalComputationHatchProvider(), MetaMachineBlock::class.java)
    }
}