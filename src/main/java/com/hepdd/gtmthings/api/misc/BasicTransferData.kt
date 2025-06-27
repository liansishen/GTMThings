package com.hepdd.gtmthings.api.misc

import com.gregtechceu.gtceu.api.machine.MetaMachine
import java.util.UUID

data class BasicTransferData(var uUID: UUID, var throughput: Long, var machine: MetaMachine):ITransferData {
    override fun UUID(): UUID? {
        return uUID
    }

    override fun throughput(): Long {
        return throughput
    }

    override fun machine(): MetaMachine? {
        return machine
    }
}
