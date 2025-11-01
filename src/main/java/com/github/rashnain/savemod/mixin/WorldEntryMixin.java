package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.Identifier;
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

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin extends WorldListWidget.Entry {

    @Shadow @Final LevelSummary level;

    @Shadow @Final private WorldListWidget parent;

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"))
    public void mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        SaveMod.worldDir = level.getName();
        if (click.x() - (getContentX() + getContentWidth() - 32) >= 0 && SaveModConfig.worldEntries.getValue())
            MinecraftClient.getInstance().setScreen(new SelectSaveScreen(parent.getParent(), () -> parent.refresh()));
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget$Entry;keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", shift = At.Shift.BEFORE), cancellable = true)
    public void keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (input.getKeycode() == 262 || input.getKeycode() == 326) {
            SaveMod.worldDir = level.getName();
            MinecraftClient.getInstance().setScreen(new SelectSaveScreen(parent.getParent(), () -> parent.refresh()));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", shift = At.Shift.AFTER))
    public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (SaveModConfig.worldEntries.getValue()) {
            int pixelsAfterSaveListButton = mouseX - (getContentX() + getContentWidth() - 32);
            Identifier texture = pixelsAfterSaveListButton >= 0 ? JOIN_HIGHLIGHTED_TEXTURE : JOIN_TEXTURE;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, getContentX() + getContentWidth() - 32, getContentY(), 32, 32);
        }
    }

    @Inject(method = "delete", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;deleteSessionLock()V", shift = At.Shift.AFTER))
    public void delete(CallbackInfo ci) {
        File saveDir = SaveMod.DIR.resolve(SaveMod.worldDir).toFile();
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
            SaveMod.LOGGER.error("Could not delete save folder '{}' : {}", SaveMod.worldDir, e);
        }
    }

}
