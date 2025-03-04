package com.hepdd.gtmthings.api.misc;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import java.util.UUID;

public record BasicTransferData(UUID UUID, long Throughput, MetaMachine machine) implements ITransferData {}
