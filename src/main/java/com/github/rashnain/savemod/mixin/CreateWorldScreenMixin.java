package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @WrapOperation(method = "createNewWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;createNewWorldDirectory(Lnet/minecraft/client/Minecraft;Ljava/lang/String;Ljava/nio/file/Path;)Ljava/util/Optional;"))
    private Optional<LevelStorageSource.LevelStorageAccess> wrapCreateNewWorldDirectory(Minecraft targetDir, String files, @Nullable Path e, Operation<Optional<LevelStorageSource.LevelStorageAccess>> original) {
        Optional<LevelStorageSource.LevelStorageAccess> value = original.call(targetDir, files, e);
        value.ifPresent(v -> SaveMod.worldDir = v.getLevelId());
        return value;
    }
}
