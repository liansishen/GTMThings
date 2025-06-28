package com.hepdd.gtmthings.mixin

import net.minecraft.world.item.ItemStack

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.hepdd.gtmthings.api.machine.trait.ProgrammableCircuitHandler
import com.hepdd.gtmthings.data.CustomItems
import it.unimi.dsi.fastutil.ints.Int2IntFunction
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(SimpleTieredMachine::class)
open class SimpleTieredMachineMixin(holder: IMachineBlockEntity, tier: Int, tankScalingFunction: Int2IntFunction, vararg args: Any?) : WorkableTieredMachine(holder, tier, tankScalingFunction, *args) {

    @Inject(method = ["createCircuitItemHandler"], at = [At("HEAD")], remap = false, cancellable = true)
    protected fun createCircuitItemHandler(cir: CallbackInfoReturnable<NotifiableItemStackHandler?>) {
        cir.setReturnValue(ProgrammableCircuitHandler(this))
    }

    override fun createImportItemHandler(vararg args: Any): NotifiableItemStackHandler = NotifiableItemStackHandler(
        this,
        recipeType.getMaxInputs(ItemRecipeCapability.CAP),
        IO.IN,
    ).setFilter { i: ItemStack? -> !i!!.`is`(CustomItems.VIRTUAL_ITEM_PROVIDER.get()) || !i.hasTag() }
}
