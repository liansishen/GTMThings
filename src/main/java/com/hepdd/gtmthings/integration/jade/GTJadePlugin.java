package com.hepdd.gtmthings.integration.jade;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import com.hepdd.gtmthings.integration.jade.provider.WirelessEnergyHatchProvider;
import com.hepdd.gtmthings.integration.jade.provider.WirelessOpticalComputationHatchProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new WirelessEnergyHatchProvider(), MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(new WirelessOpticalComputationHatchProvider(), MetaMachineBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new WirelessEnergyHatchProvider(), MetaMachineBlock.class);
        registration.registerBlockComponent(new WirelessOpticalComputationHatchProvider(), MetaMachineBlock.class);
    }
}
