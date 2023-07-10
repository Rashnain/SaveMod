package net.rashnain.savemod.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.rashnain.savemod.SaveMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;clearDataPackTempDir()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void startServer(LevelProperties.SpecialProperty specialProperty, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, Lifecycle lifecycle, CallbackInfo ci, Optional<LevelStorage.Session> optional) {
        optional.ifPresent(session -> SaveMod.worldDir = session.getDirectoryName());
    }

}
