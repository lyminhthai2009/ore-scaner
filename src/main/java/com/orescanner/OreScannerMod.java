package com.orescanner;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class - Khởi tạo mod (server + client)
 * Mod này chủ yếu chạy client-side nên logic nằm ở OreScannerClient
 */
public class OreScannerMod implements ModInitializer {
    public static final String MOD_ID = "orescanner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Ore Scanner Mod initialized!");
    }
}