package com.github.rashnain.savemod.config;

import com.github.rashnain.savemod.gui.OptionsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {

    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return OptionsScreen::new;
    }

}
