package triangled.overlaymod.client;

import net.fabricmc.api.ClientModInitializer;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.hud.ClockHud;
import triangled.overlaymod.client.hud.CompassHud;
import triangled.overlaymod.client.hud.CoordinatesHud;

public class OverlayModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		OverlayModConfig.INSTANCE.preload();
		HudManager.register(new CoordinatesHud());
		HudManager.register(new CompassHud());
		HudManager.register(new ClockHud());
	}
}