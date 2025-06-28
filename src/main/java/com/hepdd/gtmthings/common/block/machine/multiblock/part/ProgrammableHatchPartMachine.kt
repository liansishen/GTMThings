package com.hepdd.gtmthings.common.block.machine.multiblock.part

import net.minecraft.world.item.ItemStack

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine
import com.hepdd.gtmthings.api.machine.trait.ProgrammableCircuitHandler
import com.hepdd.gtmthings.data.CustomItems

class ProgrammableHatchPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO, vararg args: Any?) : DualHatchPartMachine(holder, tier, io, *args) {

    override fun createInventory(vararg args: Any): NotifiableItemStackHandler = NotifiableItemStackHandler(
        this,
        inventorySize,
        io,
    ).setFilter { itemStack: ItemStack? -> !itemStack!!.`is`(CustomItems.VIRTUAL_ITEM_PROVIDER.get()) }

    override fun createCircuitItemHandler(vararg args: Any?): NotifiableItemStackHandler = if (args.isNotEmpty() && args[0] is IO && io == IO.IN) {
        ProgrammableCircuitHandler(this)
    } else {
        NotifiableItemStackHandler(this, 0, IO.NONE)
    }
}
