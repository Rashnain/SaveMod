package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"))
    private void createPauseMenu(CallbackInfo ci, @Local(name = "helper") GridLayout.RowHelper helper) {
        if (SaveModConfig.gameMenu.get() && minecraft.hasSingleplayerServer() && !minecraft.getSingleplayerServer().isPublished()) {
            helper.addChild(Button.builder(Component.translatable("savemod.list.title"), _ ->
                minecraft.gui.setScreen(new SelectSaveScreen(this))
            ).width(204).build(), 2);
        }
    }

}
