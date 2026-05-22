package dev.maksg.playclock.mc261x.mixin.screen;

import dev.maksg.playclock.mc261x.client.PlayClock261xServerListOverlayRenderer;
import dev.maksg.playclock.mc261x.client.PlayClock261xWorldListOverlayRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void playclock$beginServerOverlayFrame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if ((Object) this instanceof JoinMultiplayerScreen) {
            PlayClock261xServerListOverlayRenderer.beginFrame();
        } else if ((Object) this instanceof SelectWorldScreen) {
            PlayClock261xWorldListOverlayRenderer.beginFrame();
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void playclock$renderServerOverlayPanels(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if ((Object) this instanceof JoinMultiplayerScreen multiplayerScreen) {
            PlayClock261xServerListOverlayRenderer.renderPanels(graphics, multiplayerScreen.width, multiplayerScreen.height);
        } else if ((Object) this instanceof SelectWorldScreen selectWorldScreen) {
            PlayClock261xWorldListOverlayRenderer.renderPanels(graphics, selectWorldScreen.width, selectWorldScreen.height);
        }
    }
}
