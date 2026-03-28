package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private <T extends LayoutElement> T wrapAddChild(GridLayout.RowHelper instance, T widget, int columnWidth, Operation<T> original) {
        T value = original.call(instance, widget, columnWidth);

        if (SaveModConfig.gameMenu.get() && minecraft.hasSingleplayerServer()) {
            original.call(instance, Button.builder(Component.translatable("savemod.list.title"), button ->
                    minecraft.setScreen(new SelectSaveScreen(this))
            ).width(204).build(), 2);
        }

        return value;
    }
}
