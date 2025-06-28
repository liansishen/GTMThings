package com.hepdd.gtmthings.api.capability

interface IGTMTJadeIF {
    fun isTransmitter(): Boolean

    fun isbinded(): Boolean

    fun getBindPos(): String?
}
