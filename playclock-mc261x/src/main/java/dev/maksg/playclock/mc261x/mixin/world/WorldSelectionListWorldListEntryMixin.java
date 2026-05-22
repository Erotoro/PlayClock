package dev.maksg.playclock.mc261x.mixin.world;

import dev.maksg.playclock.core.identity.WorldIdentityResolver;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.mc261x.client.PlayClock261xWorldListOverlayRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
abstract class WorldSelectionListWorldListEntryMixin {

    @Shadow
    public abstract LevelSummary getLevelSummary();

    @Inject(method = "extractContent", at = @At("TAIL"))
    @SuppressWarnings("all")
    private void playclock$renderBadge(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
        LevelSummary level = getLevelSummary();
        if (level == null) {
            return;
        }

        TrackedTarget target = WorldIdentityResolver.resolve(level.getLevelId(), level.getLevelName(), null);
        WorldSelectionList.Entry self = (WorldSelectionList.Entry) (Object) this;
        PlayClock261xWorldListOverlayRenderer.renderDailyBadge(
                graphics,
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
