package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class TestCover extends CoverBehavior {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TestCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    private TickableSubscription subscription;

    public TestCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach() {
        var targetMachine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
//        SimpleTieredMachine stMachine;
        if (targetMachine instanceof SimpleTieredMachine) {
//            stMachine = (SimpleTieredMachine) targetMachine;
//            stMachine.getCircuitInventory()
            return true;
        } else {
            return false;
        }
//        return super.canAttach();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void update(){

        if (coverHolder.getOffsetTimer() % 5 == 0) {
            if (!(MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos()) instanceof SimpleTieredMachine)) {return;}
            SimpleTieredMachine machine = (SimpleTieredMachine) MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());

            if (machine != null) {

                ItemStack is = machine.importItems.getStackInSlot(0);

                if (is != null && is != ItemStack.EMPTY) {
                    //
                    machine.getCircuitInventory().storage.setStackInSlot(0,is);
                    machine.importItems.storage.setStackInSlot(0,ItemStack.EMPTY);
                }

            }
        }
    }


}
