package com.hepdd.gtmthings.api.misc;

import java.util.UUID;

public record BasicTransferData(UUID UUID, long Throughput) implements ITransferData {}
