package com.hepdd.gtmthings.api.capability

import java.util.*

interface IBindable {
    fun getUUID(): UUID?

    fun cover(): Boolean = false

    fun display(): Boolean = true
}
