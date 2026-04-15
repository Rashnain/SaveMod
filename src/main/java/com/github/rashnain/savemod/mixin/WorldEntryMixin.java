package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.rashnain.savemod.gui.widget.SaveListEntry.JOIN_HIGHLIGHTED_TEXTURE;
import static com.github.rashnain.savemod.gui.widget.SaveListEntry.JOIN_TEXTURE;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldEntryMixin extends WorldSelectionList.Entry {

    @Shadow @Final LevelSummary summary;

    @Shadow @Final private WorldSelectionList list;

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"))
    public void mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (SaveModConfig.worldEntries.get() && mouseButtonEvent.x() - (getContentX() + getContentWidth() - 32) >= 0) {
            SaveMod.worldDir = summary.getLevelId();
            Minecraft.getInstance().setScreen(new SelectSaveScreen(list.getScreen(), () -> list.returnToScreen()));
        }
    }

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    public void keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (keyEvent.input() == 262) {
            SaveMod.worldDir = summary.getLevelId();
            Minecraft.getInstance().setScreen(new SelectSaveScreen(list.getScreen(), () -> list.returnToScreen()));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci) {
        if (SaveModConfig.worldEntries.get()) {
            int pixelsAfterSaveListButton = mouseX - (getContentX() + getContentWidth() - 32);
            Identifier texture = pixelsAfterSaveListButton >= 0 ? JOIN_HIGHLIGHTED_TEXTURE : JOIN_TEXTURE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, getContentX() + getContentWidth() - 32, getContentY(), 32, 32);
        }
    }

    @Inject(method = "doDeleteWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;deleteLevel()V"))
    public void doDeleteWorld(CallbackInfo ci, @Local LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        String worldDir = levelStorageAccess.getLevelId();
        File saveDir = SaveMod.DIR.resolve(worldDir).toFile();
        try {
            File[] files = saveDir.listFiles(file -> file.isFile() && file.getName().endsWith(".zip") && file.getName().length() > 24);
            if (files != null) {
                for (File save : files)
                    Files.delete(save.toPath());
                Files.delete(saveDir.toPath());
                try {
                    Files.delete(SaveMod.DIR);
                } catch (IOException ignored) {}
            }
        } catch (IOException e) {
            SaveMod.LOGGER.error("Could not delete save folder '{}' : {}", worldDir, e);
        }
    }

}
