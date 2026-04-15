package com.github.rashnain.savemod.mixin;

import com.github.rashnain.savemod.SaveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "doWorldLoad", at = @At(value = "HEAD"))
    public void doWorldLoad(final LevelStorageSource.LevelStorageAccess levelSourceAccess,
                            final PackRepository packRepository, final WorldStem worldStem,
                            final Optional<GameRules> gameRules, final boolean newWorld, CallbackInfo ci) {
        SaveMod.worldDir = levelSourceAccess.getLevelId();
    }

}
