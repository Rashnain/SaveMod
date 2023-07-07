package net.rashnain.savemod.mixin;

import net.minecraft.world.level.storage.LevelStorage;
import net.rashnain.savemod.SaveMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;

@Mixin(LevelStorage.Session.class)
public abstract class SessionMixin {

    @Inject(at = @At("RETURN"), method = "createBackup", locals = LocalCapture.CAPTURE_FAILHARD)
    public void createBackup(CallbackInfoReturnable<Long> cir, String string, Path path, Path path2) {
        SaveMod.backupName = path2.getFileName().toString();
    }

}
