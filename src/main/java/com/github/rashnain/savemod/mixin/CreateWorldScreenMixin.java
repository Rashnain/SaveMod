package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;createIntegratedServerLoader()Lnet/minecraft/server/integrated/IntegratedServerLoader;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void startServer(LevelProperties.SpecialProperty specialProperty, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, Lifecycle lifecycle, CallbackInfo ci, Optional<LevelStorage.Session> optional) {
        SaveMod.worldDir = optional.get().getDirectoryName();
    }

}
