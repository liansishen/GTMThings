package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;

public class ItemProgrammingCircuit extends ComponentItem {

    public ItemProgrammingCircuit(Properties properties) {
        super(properties);
    }

    // @Override
    // public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    // consumer.accept(new IClientItemExtensions() {
    // @Override
    // public BlockEntityWithoutLevelRenderer getCustomRenderer() {
    // return new ProgrammingCircuitItemRenderer();
    // }
    // });
    // }
    //
    // @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
    // public static class ModEventBus{
    // @SubscribeEvent
    // public static void onModelBaked(ModelEvent.ModifyBakingResult event){
    // // wrench item model
    // Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
    // ModelResourceLocation location = new
    // ModelResourceLocation(BuiltInRegistries.ITEM.getKey(CustomItems.PROGRAMER_CIRCUIT.get()), "inventory");
    // BakedModel existingModel = modelRegistry.get(location);
    // if (existingModel == null) {
    // throw new RuntimeException("Did not find Obsidian Hidden in registry");
    // } else if (existingModel instanceof ProgrammingCircuitItemModel) {
    // throw new RuntimeException("Tried to replaceObsidian Hidden twice");
    // } else {
    // ProgrammingCircuitItemModel obsidianWrenchBakedModel = new ProgrammingCircuitItemModel(existingModel);
    // event.getModels().put(location, obsidianWrenchBakedModel);
    // }
    // }
    // }
}
