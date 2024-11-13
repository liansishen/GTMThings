package com.hepdd.gtmthings.api.misc;

import net.minecraft.core.GlobalPos;

import com.mojang.datafixers.util.Pair;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public class GlobalVariableStorage {

    public static final HashMap<UUID, BigInteger> GlobalEnergy = new HashMap<>(100, 0.9f);
    public static final HashMap<UUID, Pair<GlobalPos, Long>> GlobalRate = new HashMap<>(100, 0.9f);
}
