package com.github.rashnain.savemod;

import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.NameSaveScreen;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SaveMod implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("SaveMod");

	public static final Path DIR = Path.of("savemod");

	public static String worldDir;

	@Override
	public void onInitializeClient() {
        KeyBinding.Category key_category = KeyBinding.Category.create(Identifier.of("savemod", "main"));
		KeyBinding openList = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.savemod.open_list", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, key_category));
		KeyBinding save = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.savemod.save", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, key_category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openList.isPressed() && client.isIntegratedServerRunning())
				client.setScreen(new SelectSaveScreen(null));
			if (save.isPressed() && client.isIntegratedServerRunning())
				client.setScreen(new NameSaveScreen(null, "", SaveMod.worldDir, saveName -> {
					SelectSaveScreen saveScreen = new SelectSaveScreen(null);
					client.setScreen(saveScreen);
					saveScreen.save(saveName);
				}));
		});

		SaveModConfig.load();
	}

}
