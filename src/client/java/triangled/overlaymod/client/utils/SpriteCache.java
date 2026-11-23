package triangled.overlaymod.client.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.polyfrost.compose.render.PolyBitmap;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.ResourceFinishedLoading;

public final class SpriteCache {
    private static final Map<String, Optional<PolyBitmap>> LOADED_BITMAPS = new ConcurrentHashMap<>();

    static {
        EventManager.register(ResourceFinishedLoading.class, (Runnable) LOADED_BITMAPS::clear);
    }

    private SpriteCache() {
    }

    public static PolyBitmap guiSprite(String path) {
        return texture(Identifier.withDefaultNamespace("textures/gui/sprites/" + path + ".png"));
    }

    public static PolyBitmap sprite(Identifier sprite) {
        return texture(sprite.withPath(path -> "textures/" + path + ".png"));
    }

    public static PolyBitmap guiSpriteRotated(String path) {
        return LOADED_BITMAPS
                .computeIfAbsent("rotated:" + path,
                        key -> Optional.ofNullable(guiSprite(path)).map(SpriteCache::rotateClockwise))
                .orElse(null);
    }

    private static PolyBitmap texture(Identifier texture) {
        return LOADED_BITMAPS.computeIfAbsent(texture.toString(), key -> Optional.ofNullable(load(texture)))
                .orElse(null);
    }

    private static PolyBitmap load(Identifier texture) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream stream = resource.get().open()) {
            return PolyBitmap.decode(stream.readAllBytes());
        } catch (IOException exception) {
            return null;
        }
    }

    private static PolyBitmap rotateClockwise(PolyBitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        byte[] sourcePixels = source.getPixels();
        byte[] rotatedPixels = new byte[sourcePixels.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.arraycopy(sourcePixels, (y * width + x) * 4, rotatedPixels, (x * height + (height - 1 - y)) * 4,
                        4);
            }
        }
        return new PolyBitmap(height, width, rotatedPixels);
    }
}
