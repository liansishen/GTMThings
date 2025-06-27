package com.hepdd.gtmthings.common.block.machine.multiblock.part

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEConfigSlotWidget
import com.hepdd.gtmthings.api.misc.UnlimitedItemStackTransfer
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import java.util.function.Function
import java.util.function.IntFunction

open class CreativeInputBusPartMachine(holder: IMachineBlockEntity, transferFactory: Function<Int?, ItemStackTransfer>):TieredIOPartMachine(holder, GTValues.MAX, IO.IN),IDistinctPart {
    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            CreativeInputBusPartMachine::class.java,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER
        )

        private const val ITEM_SIZE = 5
    }

    @Persisted
    private var inventory: NotifiableItemStackHandler
    protected var autoIOSubs: TickableSubscription? = null
    protected var inventorySubs: ISubscription? = null

    @Persisted
    protected var circuitInventory: NotifiableItemStackHandler? = null

    @Persisted
    private var creativeStorage: ItemStackTransfer? = null
    protected var lstItem: ArrayList<Item?>? = null

    init {
        this.inventory = createInventory()
        this.circuitInventory = createCircuitItemHandler()
        this.creativeStorage = transferFactory.apply(this.getInventorySize())
        this.lstItem = java.util.ArrayList<Item?>()
    }

    constructor(holder: IMachineBlockEntity) : this(holder, Function { size: Int? -> ItemStackTransfer(size!!) })

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    protected fun getInventorySize(): Int {
        return ITEM_SIZE * ITEM_SIZE
    }

    protected fun createInventory(): NotifiableItemStackHandler {
        return InfinityItemStackHandler(
            this,
            getInventorySize(),
            io,
            io
        ) { size: Int -> UnlimitedItemStackTransfer(size) }
    }

    protected fun createCircuitItemHandler(): NotifiableItemStackHandler {
        return NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
            .setFilter { itemStack: ItemStack? -> IntCircuitBehaviour.isIntegratedCircuit(itemStack) }
    }

    override fun onLoad() {
        super.onLoad()
        for (i in 0..<this.getInventorySize()) {
            val `is` = this.creativeStorage!!.getStackInSlot(i)
            if (!`is`.isEmpty) {
                lstItem!!.add(`is`.item)
            }
        }
        updateInventorySubscription()
    }

    override fun onUnload() {
        super.onUnload()
        if (inventorySubs != null) {
            inventorySubs!!.unsubscribe()
            inventorySubs = null
        }
    }

    override fun isDistinct(): Boolean {
        return inventory.isDistinct() && circuitInventory!!.isDistinct()
    }

    override fun setDistinct(isDistinct: Boolean) {
        inventory.setDistinct(isDistinct)
        circuitInventory!!.setDistinct(isDistinct)
    }

    protected fun autoKeep() {
        if (offsetTimer % 5 == 0L) {
            for (i in 0..<this.getInventorySize()) {
                val `is` = this.creativeStorage!!.getStackInSlot(i)
                if (!`is`.isEmpty) {
                    val newItem = `is`.copy()
                    newItem.count = Int.Companion.MAX_VALUE
                    inventory.storage.setStackInSlot(i, newItem)
                }
            }
            updateInventorySubscription()
        }
    }

    protected fun updateInventorySubscription() {
        if (!lstItem!!.isEmpty()) {
            autoIOSubs = subscribeServerTick(autoIOSubs) { this.autoKeep() }
        } else if (autoIOSubs != null) {
            autoIOSubs!!.unsubscribe()
            autoIOSubs = null
        }
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        super.setWorkingEnabled(workingEnabled)
    }

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super<TieredIOPartMachine>.attachConfigurators(configuratorPanel)
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(CircuitFancyConfigurator(circuitInventory!!.storage))
        }
    }

    override fun createUIWidget(): Widget {
        var rowSize = ITEM_SIZE
        var colSize = ITEM_SIZE
        if (getInventorySize() == 8) {
            rowSize = 4
            colSize = 2
        }
        val group = WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16)
        val container = WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8)
        var index = 0
        for (y in 0..<colSize) {
            for (x in 0..<rowSize) {
                val finalIndex = index++
                container.addWidget(
                    object : PhantomSlotWidget(this.creativeStorage, finalIndex, 4 + x * 18, 4 + y * 18) {
                        override fun slotClickPhantom(
                            slot: Slot,
                            mouseButton: Int,
                            clickTypeIn: ClickType,
                            stackHeld: ItemStack
                        ): ItemStack {
                            var stack = ItemStack.EMPTY
                            val stackSlot = slot.item
                            if (!stackSlot.isEmpty) {
                                stack = stackSlot.copy()
                            }

                            if (stackHeld.isEmpty || mouseButton == 2 || mouseButton == 1) {   // held is
                                lstItem!!.remove(stackSlot.item)
                                fillPhantomSlot(slot, ItemStack.EMPTY)
                                inventory.setStackInSlot(finalIndex, ItemStack.EMPTY)
                                updateInventorySubscription()
                            } else if (stackSlot.isEmpty) {
                                if (!stackHeld.isEmpty && !lstItem!!.contains(stackHeld.item)) { // held is not
                                    lstItem!!.add(stackHeld.item)
                                    fillPhantomSlot(slot, stackHeld)
                                    val itemStack = stackHeld.copy()
                                    itemStack.count = Int.Companion.MAX_VALUE
                                    inventory.setStackInSlot(finalIndex, itemStack)
                                    updateInventorySubscription()
                                }
                            } else {
                                if (!areItemsEqual(stackSlot, stackHeld)) {  // slot item is not equal to held item
                                    if (!lstItem!!.contains(stackHeld.item)) { // item not in another slot ->
                                        // change the slot
                                        lstItem!!.remove(stackSlot.item)
                                        lstItem!!.add(stackHeld.item)
                                        fillPhantomSlot(slot, stackHeld)
                                        val itemStack = stackHeld.copy()
                                        itemStack.count = Int.Companion.MAX_VALUE
                                        inventory.setStackInSlot(finalIndex, itemStack)
                                        updateInventorySubscription()
                                    }
                                }
                            }
                            return stack
                        }

                        override fun drawInBackground(
                            graphics: GuiGraphics,
                            mouseX: Int,
                            mouseY: Int,
                            partialTicks: Float
                        ) {
                            super.drawInBackground(graphics, mouseX, mouseY, partialTicks)
                            val position = getPosition()
                            GuiTextures.SLOT.draw(
                                graphics,
                                mouseX,
                                mouseY,
                                position.x.toFloat(),
                                position.y.toFloat(),
                                18,
                                18
                            )
                            GuiTextures.CONFIG_ARROW_DARK.draw(
                                graphics,
                                mouseX,
                                mouseY,
                                position.x.toFloat(),
                                position.y.toFloat(),
                                18,
                                18
                            )
                            val stackX = position.x + 1
                            val stackY = position.y + 1
                            var stack: ItemStack?
                            if (handler != null) {
                                stack = handler!!.item
                                DrawerHelper.drawItemStack(graphics, stack, stackX, stackY, -0x1, null)
                            }
                            if (mouseOverStock(mouseX.toDouble(), mouseY.toDouble())) {
                                AEConfigSlotWidget.drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16)
                            }
                        }

                        fun fillPhantomSlot(slot: Slot, stackHeld: ItemStack) {
                            if (stackHeld.isEmpty) {
                                slot.set(ItemStack.EMPTY)
                            } else {
                                val phantomStack = stackHeld.copy()
                                phantomStack.count = 1
                                slot.set(phantomStack)
                            }
                        }

                        override fun areItemsEqual(itemStack1: ItemStack, itemStack2: ItemStack): Boolean {
                            return ItemStack.matches(itemStack1, itemStack2)
                        }

                        fun mouseOverStock(mouseX: Double, mouseY: Double): Boolean {
                            val position = getPosition()
                            return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY)
                        }
                    }
                        .setClearSlotOnRightClick(false)
                        .setChangeListener { this.markDirty() }
                )
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE)
        group.addWidget(container)

        return group
    }

    open class InfinityItemStackHandler(
        machine: MetaMachine,
        slots: Int,
        handlerIO: IO,
        capabilityIO: IO,
        storageFactory: IntFunction<CustomItemStackHandler?>
    ) : NotifiableItemStackHandler(machine, slots, handlerIO, capabilityIO, storageFactory) {
        override fun handleRecipeInner(
            io: IO,
            recipe: GTRecipe,
            left: MutableList<Ingredient?>,
            simulate: Boolean
        ): MutableList<Ingredient?>? {
            return super.handleRecipeInner(io, recipe, left, true)
        }
    }
}