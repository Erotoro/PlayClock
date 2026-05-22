package dev.maksg.playclock.mc261x.mixin.multiplayer;

import dev.maksg.playclock.core.identity.ServerIdentityNormalizer;
import dev.maksg.playclock.core.model.SourceType;
import dev.maksg.playclock.core.model.TrackedTarget;
import dev.maksg.playclock.mc261x.client.PlayClock261xServerListOverlayRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.server.LanServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.NetworkServerEntry.class)
abstract class ServerSelectionListNetworkServerEntryMixin {

    @Shadow
    protected LanServer serverData;

    @Inject(method = "extractContent", at = @At("TAIL"))
    @SuppressWarnings("all")
    private void playclock$renderBadge(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
        if (serverData == null || serverData.getAddress() == null || serverData.getAddress().isBlank()) {
            return;
        }

        TrackedTarget target = ServerIdentityNormalizer.normalize(serverData.getAddress(), SourceType.LAN);
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
