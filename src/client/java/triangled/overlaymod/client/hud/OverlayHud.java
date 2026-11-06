package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;

public abstract class OverlayHud extends LegacyHud {
    protected OverlayHud(String id, String title, Hud.Category category) {
        super(id, title, category);
    }

    public static <T extends Hud> List<T> activeHuds(Class<T> type) {
        List<T> huds = new ArrayList<>();
        for (Hud hud : HudManager.INSTANCE.getActiveInstances()) {
            if (type.isInstance(hud)) {
                huds.add(type.cast(hud));
            }
        }
        return huds;
    }

    protected static boolean isEditing() {
        return HudManager.INSTANCE.isEditing();
    }

    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    protected static LocalPlayer player() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? null : mc.player;
    }

    @Override
    public boolean update() {
        return false;
    }
}
