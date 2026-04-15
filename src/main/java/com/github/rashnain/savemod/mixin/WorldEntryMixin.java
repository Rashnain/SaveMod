package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.rashnain.savemod.gui.widget.SaveListEntry.JOIN_HIGHLIGHTED_TEXTURE;
import static com.github.rashnain.savemod.gui.widget.SaveListEntry.JOIN_TEXTURE;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin extends WorldListWidget.Entry implements AutoCloseable {

    @Shadow @Final LevelSummary level;

    @Shadow @Final private SelectWorldScreen screen;

    @Unique private int x;

    @Unique private int entryWidth;

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"))
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (SaveModConfig.worldEntries.getValue() && mouseX - (x + entryWidth - 32) >= 0) {
            SaveMod.worldDir = level.getName();
            MinecraftClient.getInstance().setScreen(new SelectSaveScreen(screen));
        }
    }

    @Override @Unique
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (keyCode == 262) {
            SaveMod.worldDir = level.getName();
            MinecraftClient.getInstance().setScreen(new SelectSaveScreen(screen));
            return true;
        }

        return false;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (SaveModConfig.worldEntries.getValue()) {
            this.x = x;
            this.entryWidth = entryWidth;
            int pixelsAfterSaveListButton = mouseX - (x + entryWidth - 32);
            Identifier texture = pixelsAfterSaveListButton >= 0 ? JOIN_HIGHLIGHTED_TEXTURE : JOIN_TEXTURE;
            context.drawGuiTexture(texture, x + entryWidth - 32, y, 32, 32);
        }
    }

    @Inject(method = "delete", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;deleteSessionLock()V"))
    public void delete(CallbackInfo ci, @Local LevelStorage.Session session) {
        String worldDir = session.getDirectoryName();
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
