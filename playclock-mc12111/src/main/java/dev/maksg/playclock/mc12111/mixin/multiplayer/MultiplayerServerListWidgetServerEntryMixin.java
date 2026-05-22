package dev.maksg.playclock.mc12111.mixin.multiplayer;

import dev.maksg.playclock.core.identity.ServerIdentityNormalizer;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.mc12111.client.PlayClock12111ServerListOverlayRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
abstract class MultiplayerServerListWidgetServerEntryMixin {

    @Shadow
    public abstract ServerInfo getServer();

    @Inject(method = "render", at = @At("TAIL"))
    @SuppressWarnings("all")
    private void playclock$renderBadge(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickProgress, CallbackInfo ci) {
        ServerInfo server = getServer();
        if (server == null || server.address == null || server.address.isBlank()) {
            return;
        }

        TrackedTarget target = ServerIdentityNormalizer.normalize(server.address, SourceType.SAVED);
        MultiplayerServerListWidget.Entry self = (MultiplayerServerListWidget.Entry) (Object) this;
        PlayClock12111ServerListOverlayRenderer.renderDailyBadge(
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
