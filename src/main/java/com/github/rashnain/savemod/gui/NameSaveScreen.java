package com.github.rashnain.savemod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class NameSaveScreen extends Screen {

    private final Screen parent;
    private final String previousName;
    private final String worldName;
    private final Consumer<String> consumer;
    private EditBox nameBox;

    public NameSaveScreen(Screen parent, String previousName, String worldName, Consumer<String> consumer) {
        super(Component.empty());
        this.parent = parent;
        this.previousName = previousName;
        this.worldName = worldName;
        this.consumer = consumer;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent))
            return true;

        if (getFocused() == nameBox && keyEvent.input() == 257 || keyEvent.input() == 335) {
            consumer.accept(nameBox.getValue());
            return true;
        }

        return false;
    }

    @Override
    protected void init() {
        nameBox = new EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20, null, Component.empty());
        addRenderableWidget(nameBox);

        if (previousName != null && !previousName.equals(worldName))
            nameBox.setValue(previousName);

        Component message;
        if (previousName == null || previousName.isEmpty())
            message = Component.translatable("savemod.name.create");
        else
            message = Component.translatable("savemod.name.rename");

        addRenderableWidget(Button.builder(message, button -> consumer.accept(nameBox.getValue())
        ).bounds(width / 2 - 150 - 5, height / 2 + 25, 150, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()
        ).bounds(width / 2 + 5, height / 2 + 25, 150, 20).build());

        setInitialFocus(nameBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        if (previousName == null || previousName.isEmpty())
            guiGraphics.drawCenteredString(font, Component.translatable("savemod.name.title.new"), width / 2, height / 2 - 45, -1);
        else
            guiGraphics.drawCenteredString(font, Component.translatable("savemod.name.title.rename"), width / 2, height / 2 - 45, -1);
        guiGraphics.drawCenteredString(font, Component.translatable("savemod.name.hint", worldName), width / 2, height / 2 - 30, -0x808080);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

}
