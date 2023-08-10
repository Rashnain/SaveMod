package net.rashnain.savemod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.level.storage.LevelSummary;
import net.rashnain.savemod.SaveMod;
import net.rashnain.savemod.config.SaveModConfig;
import net.rashnain.savemod.gui.SelectSaveScreen;
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
import java.nio.file.Path;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldEntryMixin extends WorldListWidget.Entry implements AutoCloseable {

    @Shadow @Final private LevelSummary level;

    @Shadow @Final private SelectWorldScreen screen;

    @Unique private int x = 80;

    @Unique private int entryWidth = 270;

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/WorldListWidget;setSelected(Lnet/minecraft/client/gui/screen/world/WorldListWidget$Entry;)V", shift = At.Shift.AFTER))
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        SaveMod.worldDir = level.getName();
        if (mouseX - (x + entryWidth - 32) >= 0 && SaveModConfig.worldEntries.getValue())
            MinecraftClient.getInstance().setScreen(new SelectSaveScreen(screen));
    }

    @Override @Unique
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (keyCode == 262 || keyCode == 326) {
            SaveMod.worldDir = level.getName();
            MinecraftClient.getInstance().setScreen(new SelectSaveScreen(screen));
            return true;
        }

        return false;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", shift = At.Shift.AFTER))
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (SaveModConfig.worldEntries.getValue()) {
            this.x = x;
            this.entryWidth = entryWidth;
            int pixelsAfterSaveListButton = mouseX - (x + entryWidth - 32);
            float textureY = pixelsAfterSaveListButton >= 0 ? 32 : 0;
            DrawableHelper.drawTexture(matrices, x + entryWidth - 32, y, 0, textureY, 32, 32, 256, 256);
        }
    }

    @Inject(method = "delete", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;deleteSessionLock()V", shift = At.Shift.AFTER))
    public void delete(CallbackInfo ci) {
        File savesDir = new File(Path.of("savemod").resolve(SaveMod.worldDir).toUri());
        try {
            File[] files = savesDir.listFiles();
            if (files != null) {
                for (File save : files)
                    Files.deleteIfExists(save.toPath());
                Files.deleteIfExists(savesDir.toPath());
            }
        } catch (IOException e) {
            SaveMod.LOGGER.error("Could not delete save of '{}' : {}", SaveMod.worldDir, e);
        }
    }

}
