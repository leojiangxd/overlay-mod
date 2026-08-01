package triangled.overlaymod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import triangled.overlaymod.HUD.ClockHUD;
import triangled.overlaymod.HUD.CoordinatesHUD;
import triangled.overlaymod.config.OverlayModConfig;

public class OverlayMod implements ClientModInitializer {
	public static final String MOD_ID = "overlay-mod";

	public static OverlayModConfig config;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(OverlayModConfig.class, GsonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig();

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("coordinates_hud"), CoordinatesHUD::render);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("clock_hud"), ClockHUD::render);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
