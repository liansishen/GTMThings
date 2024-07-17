package com.hepdd.gtmthings.worldgen;

import com.google.common.collect.ImmutableList;
import com.hepdd.gtmthings.GTMThings;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class DimensionHelper {

    public static final ResourceKey<DimensionType> PERSONALDIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(GTMThings.MOD_ID, "personaldim_type"));

    public DimensionHelper() {}

    @SuppressWarnings("deprecation")
    public static Optional<ServerLevel> getWorld(MinecraftServer server, int index) {
        if (server == null) return Optional.empty();
        if (index <= 0) {
            return Optional.of(server.overworld());
        }
        ResourceKey<Level> world = getRegistryKey(index);
        return Optional.ofNullable(server.forgeGetWorldMap().get(world));
    }

    @SuppressWarnings("deprecation")
    public static ServerLevel getOrCreateWorld(MinecraftServer server, int index,Map<Block,Integer> layerMap) {
        return getWorld(server, index).orElseGet(() -> {
            Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();
            ResourceKey<Level> world = getRegistryKey(index);
            return createAndRegisterWorldAndDimension(server, map, world, layerMap);
        });
    }

    @SuppressWarnings("deprecation")
    public static int getWorldCount(MinecraftServer server) {
        return server.forgeGetWorldMap().size();
    }


    public static ResourceKey<Level> getRegistryKey(int index) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GTMThings.MOD_ID, "DIM" + String.valueOf(index)));
    }

    public static int getIndex(ResourceKey<Level> world) {
        if (world.location().getNamespace().equals(GTMThings.MOD_ID)) {
            return Integer.parseInt(world.location().getPath());
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    private static ServerLevel createAndRegisterWorldAndDimension(MinecraftServer server, Map<ResourceKey<Level>, ServerLevel> map, ResourceKey<Level> worldKey, Map<Block,Integer> layerMap) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, worldKey.location());
        LevelStem dimension = createDimension(server,layerMap);

        WorldData worldData = server.getWorldData();
        WorldOptions worldGenSettings = worldData.worldGenOptions();

        DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

        LayeredRegistryAccess<RegistryLayer> registries = server.registries();
        RegistryAccess.ImmutableRegistryAccess composite = (RegistryAccess.ImmutableRegistryAccess) registries.compositeAccess();
        Map<ResourceKey<? extends Registry<?>>, Registry<?>> regmap = new HashMap<>(composite.registries);
        MappedRegistry<LevelStem> oldRegistry = (MappedRegistry<LevelStem>) regmap.get(Registries.LEVEL_STEM);
        Lifecycle oldLifecycle = oldRegistry.registryLifecycle();
        MappedRegistry<LevelStem> newRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, oldLifecycle, false);
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : oldRegistry.entrySet()) {
            ResourceKey<LevelStem> oldKey = entry.getKey();
            ResourceKey<Level> oldLevelKey = ResourceKey.create(Registries.DIMENSION, oldKey.location());
            LevelStem dim = entry.getValue();
            if (dim != null && oldLevelKey != worldKey) {
                Registry.register(newRegistry, oldKey, dim);
            }
        }
        Registry.register(newRegistry, dimensionKey, dimension);
        regmap.replace(Registries.LEVEL_STEM, newRegistry);
        composite.registries = regmap;

        ServerLevel newWorld = new ServerLevel(
                server,
                server.executor,
                server.storageSource,
                derivedLevelData,
                worldKey,
                dimension,
                server.progressListenerFactory.create(11),
                worldData.isDebugWorld(),
                BiomeManager.obfuscateSeed(worldGenSettings.seed()),
                ImmutableList.of(),
                false,
                null
        );
        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newWorld.getWorldBorder()));
        map.put(worldKey, newWorld);
        server.markWorldsDirty();
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(newWorld));
        return newWorld;
    }

    public static LevelStem createDimension(MinecraftServer server,Map<Block,Integer> layerMap) {

        RegistryAccess access = server.registryAccess();
        Holder<DimensionType> dimType = access.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(PERSONALDIM_TYPE);
        Holder<Biome> biome = access.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);

        FlatLevelGeneratorSettings flatChunkGeneratorConfig =
                new FlatLevelGeneratorSettings(
                        Optional.of(HolderSet.direct()),
                        biome,
                        List.of()
                );

        var lays = flatChunkGeneratorConfig.getLayersInfo();
        for(Block key:layerMap.keySet()) {
            lays.add(new FlatLayerInfo(layerMap.get(key),key));
        }
        if (lays.isEmpty()) {
            lays.add(new FlatLayerInfo(1, Blocks.AIR));
        }
//        lays.add(new FlatLayerInfo(1, Blocks.BEDROCK));
//        lays.add(new FlatLayerInfo(1, Blocks.SMOOTH_STONE));
//        lays.add(new FlatLayerInfo(20, Blocks.AIR));
//        lays.add(new FlatLayerInfo(3, Blocks.SMOOTH_STONE));
        flatChunkGeneratorConfig.updateLayers();


        ChunkGenerator flatLevelSource = new FlatLevelSource(flatChunkGeneratorConfig);

        return new LevelStem(dimType, flatLevelSource);
    }

}
