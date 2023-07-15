package net.rashnain.savemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.rashnain.savemod.config.SaveModConfig;
import net.rashnain.savemod.gui.NameSaveScreen;
import net.rashnain.savemod.gui.SelectSaveScreen;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveMod implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("SaveMod");

	public static String worldDir;

	@Override
	public void onInitializeClient() {
		KeyBinding openList = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.savemod.open_list", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.savemod"));
		KeyBinding save = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.savemod.save", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.savemod"));

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
