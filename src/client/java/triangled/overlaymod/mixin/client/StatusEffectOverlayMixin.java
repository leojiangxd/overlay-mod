package triangled.overlaymod.mixin.client;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import me.shedaniel.autoconfig.AutoConfig;

import triangled.overlaymod.config.OverlayModConfig;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

@Mixin(InGameHud.class)
public class StatusEffectOverlayMixin {
}
