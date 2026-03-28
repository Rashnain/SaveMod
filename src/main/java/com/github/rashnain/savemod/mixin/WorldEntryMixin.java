package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
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

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldEntryMixin extends WorldSelectionList.Entry {
    @Shadow @Final private LevelSummary summary;
    @Shadow @Final private WorldSelectionList list;

    @Unique private static final Identifier SAVE_LIST_HIGHLIGHTED_TEXTURE = SaveMod.id("save_list_highlighted");
    @Unique private static final Identifier SAVE_LIST_TEXTURE = SaveMod.id("save_list");

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"))
    public void mouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        SaveMod.worldDir = summary.getLevelId();
        if (click.x() - (getContentX() + getContentWidth() - 32) >= 0 && SaveModConfig.worldEntries.get())
            Minecraft.getInstance().setScreen(new SelectSaveScreen(list.getScreen(), () -> list.returnToScreen()));
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList$Entry;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", shift = At.Shift.BEFORE), cancellable = true)
    public void keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (input.input() == 262 || input.input() == 326) {
            SaveMod.worldDir = summary.getLevelId();
            Minecraft.getInstance().setScreen(new SelectSaveScreen(list.getScreen(), () -> list.returnToScreen()));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void injectExtractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (SaveModConfig.worldEntries.get() && hovered) {
            int pixelsAfterSaveListButton = mouseX - (getContentX() + getContentWidth() - 32);
            Identifier texture = pixelsAfterSaveListButton >= 0 ? SAVE_LIST_HIGHLIGHTED_TEXTURE : SAVE_LIST_TEXTURE;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, getContentX() + getContentWidth() - 32, getContentY(), 32, 32);
        }
    }

    @Inject(method = "doDeleteWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;deleteLevel()V", shift = At.Shift.AFTER))
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
