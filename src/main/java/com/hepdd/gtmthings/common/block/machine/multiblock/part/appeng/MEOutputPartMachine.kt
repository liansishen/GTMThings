package com.hepdd.gtmthings.common.block.machine.multiblock.part.appeng

import net.minecraft.core.Direction

import appeng.api.config.Actionable
import appeng.api.networking.IGridNodeListener
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine
import com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage
import com.hepdd.gtmthings.api.machine.trait.InaccessibleInfiniteHandler
import com.hepdd.gtmthings.api.machine.trait.InaccessibleInfiniteTank
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.lowdragmc.lowdraglib.utils.Position
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap

import java.util.*

open class MEOutputPartMachine(holder: IMachineBlockEntity) :
    DualHatchPartMachine(holder, GTValues.LuV, IO.OUT),
    IInteractedMachine,
    IGridConnectedMachine {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder =
            ManagedFieldHolder(MEOutputPartMachine::class.java, DualHatchPartMachine.MANAGED_FIELD_HOLDER)
    }

    @DescSynced
    private var isMachineOnline: Boolean = false

    @Persisted
    private var nodeHolder: GridNodeHolder? = null
    private var actionSource: IActionSource? = null
    private var returnBuffer: Object2LongOpenHashMap<AEKey?> = Object2LongOpenHashMap<AEKey?>()

    @Persisted
    private var internalBuffer: KeyStorage? = null

    @Persisted
    private var internalTankBuffer: KeyStorage? = null

    init {
        this.nodeHolder = createNodeHolder()
        this.actionSource = IActionSource.ofMachine { nodeHolder!!.getMainNode().node }
    }

    protected fun createNodeHolder(): GridNodeHolder = GridNodeHolder(this)

    override fun getMainNode(): IManagedGridNode = nodeHolder!!.getMainNode()

    override fun isOnline(): Boolean = isMachineOnline

    override fun setOnline(online: Boolean) {
        isMachineOnline = online
    }

    override fun onMainNodeStateChanged(reason: IGridNodeListener.State) {
        super.onMainNodeStateChanged(reason)
        this.updateInventorySubscription()
    }

    override fun updateInventorySubscription() {
        if (shouldSubscribe()) {
            autoIOSubs = subscribeServerTick(autoIOSubs) { this.autoIO() }
        } else if (autoIOSubs != null) {
            autoIOSubs!!.unsubscribe()
            autoIOSubs = null
        }
    }

    /**/
    // /////////////////////////// */ // ***** Machine LifeCycle ****//
    /**/
    // /////////////////////////// */
    override fun createInventory(vararg args: Any?): NotifiableItemStackHandler {
        this.internalBuffer = KeyStorage()
        return InaccessibleInfiniteHandler(this, internalBuffer!!)
    }

    override fun createTank(initialCapacity: Int, slots: Int, vararg args: Any?): NotifiableFluidTank {
        this.internalTankBuffer = KeyStorage()
        return InaccessibleInfiniteTank(this, internalTankBuffer!!)
    }

    override fun onLoad() {
        super.onLoad()
        if (isRemote) return
    }

    override fun onMachineRemoved() {
        val grid = mainNode.getGrid()
        if (grid != null) {
            if (!internalBuffer!!.isEmpty) {
                for (entry in internalBuffer!!) {
                    grid.storageService.inventory.insert(
                        entry.key,
                        entry.longValue,
                        Actionable.MODULATE,
                        actionSource,
                    )
                }
            }
            if (!internalTankBuffer!!.isEmpty) {
                for (entry in internalTankBuffer!!) {
                    grid.storageService.inventory.insert(
                        entry.key,
                        entry.longValue,
                        Actionable.MODULATE,
                        actionSource,
                    )
                }
            }
        }
    }

    /**/
    // /////////////////////////// */ // ********** Sync ME *********//
    /**/
    // /////////////////////////// */
    protected fun shouldSubscribe(): Boolean = isWorkingEnabled && isOnline && (!(internalBuffer!!.storage.isEmpty() && internalTankBuffer!!.storage.isEmpty()))

    override fun autoIO() {
        if (!this.shouldSyncME()) return
        if (this.updateMEStatus()) {
            val grid = mainNode.getGrid()
            if (grid != null) {
                if (!internalBuffer!!.isEmpty) {
                    internalBuffer!!.insertInventory(grid.storageService.inventory, actionSource)
                }
                if (!internalTankBuffer!!.isEmpty) {
                    internalTankBuffer!!.insertInventory(grid.storageService.inventory, actionSource)
                }
            }
            this.updateInventorySubscription()
        }
    }

    override fun onRotated(oldFacing: Direction, newFacing: Direction) {
        super.onRotated(oldFacing, newFacing)
        mainNode.setExposedOnSides(EnumSet.of(newFacing))
    }

    override fun isWorkingEnabled(): Boolean = true

    override fun createUIWidget(): Widget {
        val group = WidgetGroup(Position(0, 0))
        // ME Network status
        group.addWidget(
            LabelWidget(
                0,
                0,
            ) { if (this.isMachineOnline) "gtceu.gui.me_network.online" else "gtceu.gui.me_network.offline" },
        )

        group.addWidget(AEListGridWidget.Item(5, 20, 3, this.internalBuffer))
        group.addWidget(AEListGridWidget.Fluid(5, 80, 3, this.internalTankBuffer))
        return group
    }

    override fun isRemote(): Boolean = holder.level()?.isClientSide ?: true
}
