package triangled.overlaymod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import triangled.overlaymod.HUD.ClockHUD;
import triangled.overlaymod.HUD.CoordinatesHUD;

public class OverlayMod implements ModInitializer {
	public static final String MOD_ID = "overlay-mod";

	@Override
	public void onInitialize() {
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID, "coordinates_hud"), CoordinatesHUD::render);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID, "clock_hud"), ClockHUD::render);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
