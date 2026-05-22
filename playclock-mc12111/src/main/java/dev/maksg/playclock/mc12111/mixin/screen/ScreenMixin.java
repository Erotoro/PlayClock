package dev.maksg.playclock.mc12111.mixin.screen;

import dev.maksg.playclock.mc12111.client.PlayClock12111ServerListOverlayRenderer;
import dev.maksg.playclock.mc12111.client.PlayClock12111WorldListOverlayRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void playclock$beginServerOverlayFrame(DrawContext context, int mouseX, int mouseY, float tickProgress, CallbackInfo ci) {
        if ((Object) this instanceof MultiplayerScreen) {
            PlayClock12111ServerListOverlayRenderer.beginFrame();
        } else if (((Object) this).getClass().getSimpleName().equals("SelectWorldScreen")) {
            PlayClock12111WorldListOverlayRenderer.beginFrame();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void playclock$renderServerOverlayPanels(DrawContext context, int mouseX, int mouseY, float tickProgress, CallbackInfo ci) {
        if ((Object) this instanceof MultiplayerScreen multiplayerScreen) {
            PlayClock12111ServerListOverlayRenderer.renderPanels(context, multiplayerScreen.width, multiplayerScreen.height);
        } else if (((Object) this).getClass().getSimpleName().equals("SelectWorldScreen")) {
            Screen self = (Screen) (Object) this;
            PlayClock12111WorldListOverlayRenderer.renderPanels(context, self.width, self.height);
        }
    }
}
