package com.example.examplemod.integration.jade;

import com.example.examplemod.integration.jade.provider.WirelessEnergyHatchProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new WirelessEnergyHatchProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new WirelessEnergyHatchProvider(), Block.class);
    }

//    static {
//        GTItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
//            if (type.harvestTags.isEmpty() || type.harvestTags.get(0).location().getNamespace().equals("minecraft"))
//                return;
//            HarvestToolProvider.registerHandler(new SimpleToolHandler(type.name, type.harvestTags.get(0),
//                    map.values().stream().filter(Objects::nonNull).filter(ItemProviderEntry::isPresent)
//                            .map(ItemProviderEntry::asItem).toArray(Item[]::new)));
//        });
//    }
}
