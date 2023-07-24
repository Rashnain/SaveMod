package net.rashnain.savemod.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import net.rashnain.savemod.config.SaveModConfig;
import net.rashnain.savemod.gui.SelectSaveScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initWidgets(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder, Text text) {
        if (SaveModConfig.gameMenu.getValue() && client.isIntegratedServerRunning()) {
            adder.add(ButtonWidget.builder(Text.translatable("savemod.list.title"), button ->
                client.setScreen(new SelectSaveScreen(this))
            ).width(204).build(), 2);
        }
    }

}
