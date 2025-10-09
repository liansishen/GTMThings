package com.hepdd.gtmthings.common.block.machine.multiblock.part.computation;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.capability.IGTMTJadeIF;
import com.hepdd.gtmthings.common.block.machine.trait.WirelessNotifiableComputationContainer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessOpticalComputationHatchMachine extends MultiblockPartMachine implements IDataStickInteractable, IGTMTJadeIF {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessOpticalComputationHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final boolean transmitter;

    @Persisted
    private BlockPos transmitterPos;
    @Persisted
    private BlockPos receiverPos;
    protected WirelessNotifiableComputationContainer computationContainer;

    public WirelessOpticalComputationHatchMachine(IMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        this.transmitter = transmitter;
        this.computationContainer = createComputationContainer(transmitter);
    }

    protected WirelessNotifiableComputationContainer createComputationContainer(Object... args) {
        IO io = IO.IN;
        if (args.length > 1 && args[args.length - 2] instanceof IO newIo) {
            io = newIo;
        }
        if (args.length > 0 && args[args.length - 1] instanceof Boolean transmitter) {
            return new WirelessNotifiableComputationContainer(this, io, transmitter);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public boolean isbinded() {
        return (this.transmitterPos != null || this.receiverPos != null);
    }

    @Override
    public String getBindPos() {
        if (this.isTransmitter() && this.receiverPos != null) {
            return this.receiverPos.toShortString();
        } else if (!this.isTransmitter() && this.transmitterPos != null) {
            return this.transmitterPos.toShortString();
        }
        return "";
    }

    private static final String KEY_TRANSMITTER = "wireless_computation_transmitter";
    private static final String KEY_RECEIVER = "wireless_computation_receiver";

    private void setTransmitterPos(BlockPos pos) {
        if (transmitterPos != null) {
            var level = getLevel();
            if (level != null) {
                if (MetaMachine.getMachine(level, transmitterPos) instanceof WirelessOpticalComputationHatchMachine machine) {
                    machine.receiverPos = null;
                }
            }
        }
        transmitterPos = pos;
    }

    private void setReceiverPos(BlockPos pos) {
        if (receiverPos != null) {
            var level = getLevel();
            if (level != null) {
                if (MetaMachine.getMachine(level, receiverPos) instanceof WirelessOpticalComputationHatchMachine machine) {
                    machine.transmitterPos = null;
                }
            }
        }
        receiverPos = pos;
    }

    private static CompoundTag createPos(BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        return posTag;
    }

    private static BlockPos getPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (isRemote()) return InteractionResult.SUCCESS;

        CompoundTag tag = dataStick.getOrCreateTag();
        BlockPos currentPos = getPos();
        if (isTransmitter()) {
            tag.put(KEY_TRANSMITTER, createPos(currentPos));
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tobind"));
        } else {
            tag.put(KEY_RECEIVER, createPos(currentPos));
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tobind"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (isRemote()) return InteractionResult.sidedSuccess(true);

        CompoundTag tag = dataStick.getTag();
        if (tag == null) return InteractionResult.PASS;

        if (isTransmitter() && tag.contains(KEY_RECEIVER, 10)) {
            BlockPos otherPos = getPos(tag.getCompound(KEY_RECEIVER));
            if (bindWith(otherPos, player)) {
                return InteractionResult.SUCCESS;
            }
        } else if (!isTransmitter() && tag.contains(KEY_TRANSMITTER, 10)) {
            BlockPos otherPos = getPos(tag.getCompound(KEY_TRANSMITTER));
            if (bindWith(otherPos, player)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private boolean bindWith(BlockPos otherPos, Player player) {
        Level level = getLevel();
        if (level == null || otherPos.equals(this.getPos())) return false;

        MetaMachine otherMachine = MetaMachine.getMachine(level, otherPos);
        if (otherMachine instanceof WirelessOpticalComputationHatchMachine otherWoch) {
            if (this.isTransmitter() == otherWoch.isTransmitter()) {
                return false;
            }
            if (isTransmitter()) {
                this.setReceiverPos(otherPos);
                otherWoch.setTransmitterPos(this.getPos());
            } else {
                this.setTransmitterPos(otherPos);
                otherWoch.setReceiverPos(this.getPos());
            }

            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_hatch.binded"));
            return true;
        }
        return false;
    }
}
