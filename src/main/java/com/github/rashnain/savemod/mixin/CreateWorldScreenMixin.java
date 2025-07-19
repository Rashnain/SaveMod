package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Inject(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;createIntegratedServerLoader()Lnet/minecraft/server/integrated/IntegratedServerLoader;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void startServer(CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, SaveProperties saveProperties, CallbackInfoReturnable<Boolean> cir, String string, GeneratorOptionsHolder generatorOptionsHolder, Optional<LevelStorage.Session> optional) {
        SaveMod.worldDir = optional.get().getDirectoryName();
    }

}
