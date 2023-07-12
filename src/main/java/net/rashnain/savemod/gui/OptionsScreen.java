package net.rashnain.savemod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.rashnain.savemod.config.SaveModConfig;

public class OptionsScreen extends GameOptionsScreen {

    private OptionListWidget optionList;

    public OptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("savemod.options"));
    }

    @Override
    protected void init() {
        optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
        optionList.addSingleOptionEntry(SaveModConfig.gameMenu);
        optionList.addSingleOptionEntry(SaveModConfig.worldEntries);
        optionList.addSingleOptionEntry(SaveModConfig.autoReload);
        addSelectableChild(optionList);

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close()
        ).dimensions(width / 2 - 100, height - 27,200, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        optionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 12, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        SaveModConfig.save();
    }

}
