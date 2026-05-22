package dev.maksg.playclock.mc261x.mixin.multiplayer;

import dev.maksg.playclock.core.identity.ServerIdentityNormalizer;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.mc261x.client.PlayClock261xServerListOverlayRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
abstract class ServerSelectionListOnlineServerEntryMixin {

    @Shadow
    public abstract ServerData getServerData();

    @Inject(method = "extractContent", at = @At("TAIL"))
    @SuppressWarnings("all")
    private void playclock$renderBadge(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
        ServerData server = getServerData();
        if (server == null || server.ip == null || server.ip.isBlank()) {
            return;
        }

        TrackedTarget target = ServerIdentityNormalizer.normalize(server.ip, SourceType.SAVED);
        ServerSelectionList.Entry self = (ServerSelectionList.Entry) (Object) this;
        PlayClock261xServerListOverlayRenderer.renderDailyBadge(
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
