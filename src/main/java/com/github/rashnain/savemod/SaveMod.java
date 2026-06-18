package com.github.rashnain.savemod;

import com.github.rashnain.savemod.config.SaveModConfig;
import com.github.rashnain.savemod.gui.NameSaveScreen;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
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
        KeyMapping.Category key_category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("savemod", "main"));
		KeyMapping openList = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.savemod.open_list", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, key_category));
		KeyMapping save = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.savemod.save", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, key_category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openList.isDown() && client.hasSingleplayerServer() && !client.getSingleplayerServer().isPublished())
				client.gui.setScreen(new SelectSaveScreen(null));
			if (save.isDown() && client.hasSingleplayerServer() && !client.getSingleplayerServer().isPublished())
				client.gui.setScreen(new NameSaveScreen(null, "", SaveMod.worldDir, saveName -> {
					SelectSaveScreen saveScreen = new SelectSaveScreen(null);
					client.gui.setScreen(saveScreen);
					saveScreen.save(saveName);
				}));
		});

		SaveModConfig.load();
	}

}
