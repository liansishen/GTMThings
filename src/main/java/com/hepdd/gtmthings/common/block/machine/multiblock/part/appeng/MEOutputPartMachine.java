package com.hepdd.gtmthings.common.block.machine.multiblock.part.appeng;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableManagedGridNode;
import com.hepdd.gtmthings.common.block.machine.trait.MEOutputHandler;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.List;

import static com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine.ME_UPDATE_INTERVAL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEOutputPartMachine extends TieredIOPartMachine
        implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(MEOutputPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }


    @Getter
    @Persisted
    @ReadOnlyManaged(
            onDirtyMethod = "onGridNodeDirty",
            serializeMethod = "serializeGridNode",
            deserializeMethod = "deserializeGridNode")
    private final SerializableManagedGridNode mainNode =
            (SerializableManagedGridNode) createMainNode()
                    .setFlags(GridFlags.REQUIRE_CHANNEL)
                    .setVisualRepresentation(getDefinition().getItem())
                    .setIdlePowerUsage(ConfigHolder.INSTANCE.compat.ae2.meHatchEnergyUsage)
                    .setInWorldNode(true)
                    .setExposedOnSides(
                            this.hasFrontFacing()
                                    ? EnumSet.of(this.getFrontFacing())
                                    : EnumSet.allOf(Direction.class))
                    .setTagName("proxy");

    protected final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);
    @Getter
    protected Object2LongOpenHashMap<AEKey> returnBuffer = new Object2LongOpenHashMap<>();
    private IGrid aeProxy;
    @DescSynced
    protected boolean isOnline;
    private final MEOutputHandler recipeHandler = new MEOutputHandler(this);
    @Nullable
    protected TickableSubscription updateSubs;

    protected IManagedGridNode createMainNode() {
        return new SerializableManagedGridNode(this, BlockEntityNodeListener.INSTANCE);
    }

    public MEOutputPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0,this::createManagedNode));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        mainNode.destroy();
    }

    // ME Start
    protected void createManagedNode() {
        this.mainNode.create(this.getLevel(), this.getPos());
    }

    protected boolean shouldSyncME() {
        return this.getOffsetTimer() % ME_UPDATE_INTERVAL == 0;
    }

    public boolean updateMEStatus() {
        if (this.aeProxy == null) {
            this.aeProxy = this.mainNode.getGrid();
        }
        if (this.aeProxy != null) {
            this.isOnline = this.mainNode.isOnline() && this.mainNode.isPowered();
        } else {
            this.isOnline = false;
        }
        return this.isOnline;
    }

    public boolean onGridNodeDirty(SerializableManagedGridNode node) {
        return node.isOnline();
    }

    public CompoundTag serializeGridNode(SerializableManagedGridNode node) {
        return node.serializeNBT();
    }

    public SerializableManagedGridNode deserializeGridNode(CompoundTag tag) {
        this.mainNode.deserializeNBT(tag);
        return this.mainNode;
    }

    @Override
    public void saveChanges() {
        this.onChanged();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateMEStatus();
        this.updateSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }
    // ME End

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return recipeHandler.getRecipeHandlers();
    }

    protected void updateSubscription() {
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {
        if (!shouldSyncME()) return;

        if (!isWorkingEnabled() && returnBuffer.isEmpty()) return;

        if (getMainNode().isActive() && !this.returnBuffer.isEmpty()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            var iterator = returnBuffer.object2LongEntrySet().fastIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var key = entry.getKey();
                var amount = entry.getLongValue();
                long inserted = StorageHelper.poweredInsert(
                        getMainNode().getGrid().getEnergyService(), aeNetwork, key, amount, actionSource);
                if (inserted >= amount) {
                    iterator.remove();
                } else {
                    entry.setValue(amount - inserted);
                }
            }
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (!forDrop) {
            var mapTag = new ListTag();
            for (Object2LongMap.Entry<AEKey> entry : returnBuffer.object2LongEntrySet()) {
                var entryTag = new CompoundTag();
                entryTag.put("key", entry.getKey().toTagGeneric());
                entryTag.putLong("value", entry.getLongValue());
                mapTag.add(entryTag);
            }
            tag.put("returnBuffer", mapTag);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        var mapTag = tag.getList("returnBuffer", Tag.TAG_COMPOUND);
        for (int i = 0; i < mapTag.size(); ++i) {
            var entryTag = mapTag.getCompound(i);
            var key = AEKey.fromTagGeneric(entryTag.getCompound("key"));
            if (key != null) {
                returnBuffer.put(key, entryTag.getLong("value"));
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(10, 15, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        return group;
    }
}
