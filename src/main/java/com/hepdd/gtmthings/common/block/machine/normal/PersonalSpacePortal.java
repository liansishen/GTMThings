package com.hepdd.gtmthings.common.block.machine.normal;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.data.CustomMachines;
import com.hepdd.gtmthings.worldgen.DimensionHelper;
import com.hepdd.gtmthings.worldgen.ModTeleporter;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PersonalSpacePortal extends MetaMachine
        implements IInteractedMachine, IBindable, IMachineLife, IFancyUIMachine, IDropSaveMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(PersonalSpacePortal.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    private UUID owner;
    @Persisted
    private int worldId;
    @Setter @Getter
    @Persisted
    private BlockPos blockPos;

    public PersonalSpacePortal(IMachineBlockEntity holder) {
        super(holder);

    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.putInt("worldId",this.worldId);
        IDropSaveMachine.super.saveToItem(tag);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        if (tag.contains("worldId")) {
            this.worldId = tag.getInt("worldId");
        }
    }

//    @Override
//    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
//        tag.putInt("worldId",this.worldId);
//        super.saveCustomPersistedData(tag, forDrop);
//    }
//
//    @Override
//    public void loadCustomPersistedData(CompoundTag tag) {
//        if (tag.contains("worldId")) {
//            this.worldId = tag.getInt("worldId");
//        }
//        super.loadCustomPersistedData(tag);
//    }

    @Override
    public UUID getUUID() {
        return owner;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (world instanceof ServerLevel serverLevel && player.canChangeDimensions() && !player.isPassenger() && !player.isVehicle()) {
            if(world.dimension() == Level.OVERWORLD) {
                if (worldId == 0) return InteractionResult.PASS;
                var dim = DimensionHelper.getOrCreateWorld(world.getServer(),worldId);
                player.changeDimension(dim,new ModTeleporter(new BlockPos(0,-39,0), true));
            } else {
                player.changeDimension(serverLevel.getServer().overworld(),new ModTeleporter(this.getBlockPos(), true));
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (owner == null) {
            owner = player.getUUID();
            return true;
        }
        if (!player.getUUID().equals(owner)) return false;
        return IFancyUIMachine.super.shouldOpenUI(player, hand, hit);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            this.owner = player.getUUID();
        }
        IMachineLife.super.onMachinePlaced(player, stack);
    }


    @Override
    public Widget createUIWidget() {
        int height = 117;
        int width = 178;
        var group = new WidgetGroup(0, 0, width + 8, height + 4);

        group.addWidget(new LabelWidget(4,4,String.format("worldId: %s",worldId)));

        if (getLevel().dimension() == Level.OVERWORLD && worldId == 0) {
            group.addWidget(new ButtonWidget(4,16,20,12,new TextTexture("create"),this::createDim));
        }

        return group;
    }

    private void createDim(ClickData clickData) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            var player = getLevel().getPlayerByUUID(owner);
            if (player != null && player.canChangeDimensions() && !player.isPassenger() && !player.isVehicle()) {
                worldId = DimensionHelper.getWorldCount(serverLevel.getServer()) + 1;
                var pos = new BlockPos(0,-39,0);
                var dim = DimensionHelper.getOrCreateWorld(serverLevel.getServer(),worldId);
                dim.setBlock(pos, CustomMachines.PERSONAL_SPACE_PORTAL.defaultBlockState(), 3);
                var desBlock = MetaMachine.getMachine(dim,pos);
                if (desBlock instanceof PersonalSpacePortal personalSpacePortal) {
                    personalSpacePortal.setBlockPos(this.getPos());
                }
                player.changeDimension(dim,new ModTeleporter(pos, true));
            }
        }
    }

}
