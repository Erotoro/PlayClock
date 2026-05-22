package dev.maksg.playclock.mc12111.mixin.world;

import dev.maksg.playclock.core.identity.WorldIdentityResolver;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.mc12111.client.PlayClock12111WorldListOverlayRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldListWidget.WorldEntry.class)
abstract class WorldListWidgetWorldEntryMixin {

    @Shadow
    public abstract LevelSummary getLevel();

    @Inject(method = "render", at = @At("TAIL"))
    @SuppressWarnings("all")
    private void playclock$renderBadge(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickProgress, CallbackInfo ci) {
        LevelSummary level = getLevel();
        if (level == null) {
            return;
        }

        TrackedTarget target = WorldIdentityResolver.resolve(level.getName(), level.getDisplayName(), null);
        WorldListWidget.Entry self = (WorldListWidget.Entry) (Object) this;
        PlayClock12111WorldListOverlayRenderer.renderDailyBadge(
                context,
                self.getContentX(),
                self.getContentY(),
                self.getWidth(),
                self.getHeight(),
                hovered,
                mouseX,
                mouseY,
                target);
    }
}
