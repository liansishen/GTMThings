package com.example.examplemod.api.misc;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public class GlobalVariableStorage {
    public static HashMap<UUID, BigInteger> GlobalEnergy = new HashMap<>(100, 0.9f);
}
