package triangled.overlaymod.client;

import net.fabricmc.api.ClientModInitializer;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.hud.ArmorHud;
import triangled.overlaymod.client.hud.BossBarHud;
import triangled.overlaymod.client.hud.ClockHud;
import triangled.overlaymod.client.hud.CompassHud;
import triangled.overlaymod.client.hud.CoordinatesHud;
import triangled.overlaymod.client.hud.SprintingHud;
import triangled.overlaymod.client.hud.StatusEffectsHud;

public class OverlayModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		OverlayModConfig.INSTANCE.preload();
		HudManager.register(new CoordinatesHud());
		HudManager.register(new CompassHud());
		HudManager.register(new ClockHud());
		HudManager.register(new SprintingHud());
		HudManager.register(new StatusEffectsHud());
		HudManager.register(new ArmorHud());
		HudManager.register(new BossBarHud());
		ChatPeek.init();
	}
}