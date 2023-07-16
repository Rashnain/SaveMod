package net.rashnain.savemod.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.rashnain.savemod.SaveMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SaveModConfig {

    public static final SimpleOption<Boolean> gameMenu = SimpleOption.ofBoolean("options.savemod.gameMenu", value -> Tooltip.of(Text.translatable("options.savemod.gameMenu.tooltip")), true);
    public static final SimpleOption<Boolean> worldEntries = SimpleOption.ofBoolean("options.savemod.worldEntries", value -> Tooltip.of(Text.translatable("options.savemod.worldEntries.tooltip")), false);
    public static final SimpleOption<Boolean> compression = SimpleOption.ofBoolean("options.savemod.compression", value -> Tooltip.of(Text.translatable("options.savemod.compression.tooltip")), true);

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("savemod.properties");
    private static final Properties properties = new Properties();

    public static void load() {
        if (Files.notExists(configPath)) {
            save();
        } else {
            try {
                properties.load(Files.newInputStream(configPath));
                gameMenu.setValue(Boolean.valueOf(properties.getProperty("show-button-on-game-menu", "true")));
                worldEntries.setValue(Boolean.valueOf(properties.getProperty("show-button-on-world-entries", "false")));
                compression.setValue(Boolean.valueOf(properties.getProperty("compress-saves", "true")));
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
        properties.setProperty("show-button-on-game-menu", String.valueOf(gameMenu.getValue()));
        properties.setProperty("show-button-on-world-entries", String.valueOf(worldEntries.getValue()));
        properties.setProperty("compress-saves", String.valueOf(compression.getValue()));
        try {
            properties.store(Files.newOutputStream(configPath), "Configuration file for SaveMod");
        } catch (IOException e) {
            SaveMod.LOGGER.error("Could not save config : {}", e.getMessage());
        }
    }

}
