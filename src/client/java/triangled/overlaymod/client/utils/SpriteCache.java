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

// Loads game textures as images and keeps them until resources reload.
public final class SpriteCache {
    private static final Map<String, Optional<PolyBitmap>> LOADED_BITMAPS = new ConcurrentHashMap<>();

    static {
        EventManager.register(ResourceFinishedLoading.class, (Runnable) LOADED_BITMAPS::clear);
    }

    private SpriteCache() {
    }

    // Returns a GUI sprite, like hud/hotbar.
    public static PolyBitmap guiSprite(String path) {
        return texture(Identifier.withDefaultNamespace("textures/gui/sprites/" + path + ".png"));
    }

    // Returns the texture for a sprite ID.
    public static PolyBitmap sprite(Identifier sprite) {
        return texture(sprite.withPath(path -> "textures/" + path + ".png"));
    }

    // Returns a GUI sprite rotated a quarter turn clockwise.
    public static PolyBitmap guiSpriteRotated(String path) {
        return LOADED_BITMAPS
                .computeIfAbsent("rotated:" + path,
                        key -> Optional.ofNullable(guiSprite(path)).map(SpriteCache::rotateClockwise))
                .orElse(null);
    }

    // Returns a texture, loading it the first time it's asked for.
    private static PolyBitmap texture(Identifier texture) {
        return LOADED_BITMAPS.computeIfAbsent(texture.toString(), key -> Optional.ofNullable(load(texture)))
                .orElse(null);
    }

    // Reads a texture from the resource packs, or null if it's missing.
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

    // Returns a copy of the image rotated a quarter turn clockwise.
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
