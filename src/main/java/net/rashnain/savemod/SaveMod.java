package net.rashnain.savemod;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveMod implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("SaveMod");

	public static String worldDir;

	public static String backupName;

	@Override
	public void onInitializeClient() {}

}
