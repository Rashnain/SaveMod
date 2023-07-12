package net.rashnain.savemod.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.rashnain.savemod.gui.OptionsScreen;

public class ModMenuCompat implements ModMenuApi {

    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return OptionsScreen::new;
    }

}
