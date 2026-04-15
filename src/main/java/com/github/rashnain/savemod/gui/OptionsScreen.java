package com.github.rashnain.savemod.gui;

import com.github.rashnain.savemod.config.SaveModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OptionsScreen extends OptionsSubScreen {

    private OptionsList optionList;

    public OptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("savemod.options"));
    }

    @Override
    protected void init() {
        optionList = new OptionsList(minecraft, width, this);
        optionList.addBig(SaveModConfig.gameMenu);
        optionList.addBig(SaveModConfig.worldEntries);
        optionList.addBig(SaveModConfig.compression);
        addWidget(optionList);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()
        ).bounds(width / 2 - 100, height - 27,200, 20).build());
    }

    @Override
    protected void addOptions() {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        optionList.render(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(font, title, width / 2, 12, -1);
    }

    @Override
    public void removed() {
        SaveModConfig.save();
    }

}
