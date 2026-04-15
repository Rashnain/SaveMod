package com.github.rashnain.savemod.config;

import com.github.rashnain.savemod.SaveMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SaveModConfig {

    public static final OptionInstance<Boolean> gameMenu = OptionInstance.createBoolean("options.savemod.gameMenu", value -> Tooltip.create(Component.translatable("options.savemod.gameMenu.tooltip")), true);
    public static final OptionInstance<Boolean> worldEntries = OptionInstance.createBoolean("options.savemod.worldEntries", value -> Tooltip.create(Component.translatable("options.savemod.worldEntries.tooltip")), false);
    public static final OptionInstance<Boolean> compression = OptionInstance.createBoolean("options.savemod.compression", value -> Tooltip.create(Component.translatable("options.savemod.compression.tooltip")), true);

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("savemod.properties");
    private static final Properties properties = new Properties();

    public static void load() {
        if (Files.notExists(configPath))
            save();
        else {
            try {
                properties.load(Files.newInputStream(configPath));
                gameMenu.set(Boolean.valueOf(properties.getProperty("show-button-on-game-menu", "true")));
                worldEntries.set(Boolean.valueOf(properties.getProperty("show-button-on-world-entries", "false")));
                compression.set(Boolean.valueOf(properties.getProperty("compress-saves", "true")));
            } catch (IOException e) {
                SaveMod.LOGGER.error("Could not load config : {}", e.getMessage());
            }
        }
    }

    public static void save() {
        if (Files.notExists(configPath)) {
            try {
                Files.createFile(configPath);
            } catch (IOException e) {
                SaveMod.LOGGER.error("Could not create config file : {}", e.getMessage());
            }
        }
        properties.clear();
        properties.setProperty("show-button-on-game-menu", String.valueOf(gameMenu.get()));
        properties.setProperty("show-button-on-world-entries", String.valueOf(worldEntries.get()));
        properties.setProperty("compress-saves", String.valueOf(compression.get()));
        try {
            properties.store(Files.newOutputStream(configPath), "Configuration file for SaveMod");
        } catch (IOException e) {
            SaveMod.LOGGER.error("Could not save config : {}", e.getMessage());
        }
    }

}
