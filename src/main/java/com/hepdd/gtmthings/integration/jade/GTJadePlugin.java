package com.hepdd.gtmthings.integration.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        registration.registerBlockDataProvider(new WirelessEnergyHatchProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new WirelessOpticalComputationHatchProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new WirelessEnergyHatchProvider(), Block.class);
        registration.registerBlockComponent(new WirelessOpticalComputationHatchProvider(), Block.class);
    }
}
