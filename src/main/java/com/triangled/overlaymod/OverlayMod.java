package com.triangled.overlaymod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import com.triangled.overlaymod.config.OverlayModConfig;

public class OverlayMod implements ModInitializer {
	public static final String MOD_ID = "overlay-mod";
	public static OverlayModConfig config;

	@Override
	public void onInitialize() {
		// Register the config
		AutoConfig.register(OverlayModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig();
	}
}