package com.github.rashnain.savemod.gui;

import com.github.rashnain.savemod.config.SaveModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class OptionsScreen extends GameOptionsScreen {

    private OptionListWidget optionList;

    public OptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("savemod.options"));
    }

    @Override
    protected void init() {
        optionList = new OptionListWidget(client, width, this);
        optionList.addSingleOptionEntry(SaveModConfig.gameMenu);
        optionList.addSingleOptionEntry(SaveModConfig.worldEntries);
        optionList.addSingleOptionEntry(SaveModConfig.compression);
        addSelectableChild(optionList);

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close()
        ).dimensions(width / 2 - 100, height - 27,200, 20).build());
    }

    @Override
    protected void addOptions() {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        optionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void removed() {
        SaveModConfig.save();
    }

}
