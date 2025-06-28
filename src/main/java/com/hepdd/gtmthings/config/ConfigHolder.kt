package com.hepdd.gtmthings.config;

import com.hepdd.gtmthings.GTMThings;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GTMThings.MOD_ID)
public class ConfigHolder {

    public static com.hepdd.gtmthings.config.ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(com.hepdd.gtmthings.config.ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }

    @Configurable
    @Configurable.Comment({ "如果启用，则需要使用无线能源绑定工具绑定电池箱或者变电站来提高无线能量传输上限。" })
    public boolean isWirelessRateEnable = true;
}
