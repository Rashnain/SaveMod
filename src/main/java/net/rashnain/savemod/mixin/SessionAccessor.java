package net.rashnain.savemod.mixin;

import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelStorage.Session.class)
public interface SessionAccessor {

    @Invoker("checkValid")
    void invokeCheckValid();

}
