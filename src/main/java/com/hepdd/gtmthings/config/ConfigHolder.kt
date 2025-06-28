package com.hepdd.gtmthings.config

import com.hepdd.gtmthings.GTMThings
import dev.toma.configuration.Configuration
import dev.toma.configuration.config.Config
import dev.toma.configuration.config.Configurable
import dev.toma.configuration.config.format.ConfigFormats

@Config(id = GTMThings.MOD_ID)
class ConfigHolder {
    @JvmField
    @Configurable
    @Configurable.Comment("如果启用，则需要使用无线能源绑定工具绑定电池箱或者变电站来提高无线能量传输上限。")
    var isWirelessRateEnable: Boolean = true

    companion object {
        @JvmField
        var INSTANCE: ConfigHolder? = null
        private val LOCK = Any()

        fun init() {
            synchronized(LOCK) {
                if (INSTANCE == null) {
                    INSTANCE =
                        Configuration.registerConfig(ConfigHolder::class.java, ConfigFormats.yaml())
                            .getConfigInstance()
                }
            }
        }
    }
}
