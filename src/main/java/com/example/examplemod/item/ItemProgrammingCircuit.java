package com.example.examplemod.item;

import com.example.examplemod.item.Render.ProgrammingCircuitItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class ItemProgrammingCircuit extends Item {
    public ItemProgrammingCircuit(Properties properties) {
        super(properties);

    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new ProgrammingCircuitItemRenderer();
            }
        });
    }
}
