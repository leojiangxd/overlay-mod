package triangled.overlaymod;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

public class OverlayMod implements ModInitializer {
	public static final String MOD_ID = "overlay-mod";

	@Override
	public void onInitialize() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
