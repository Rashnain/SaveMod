package com.github.rashnain.savemod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class NameSaveScreen extends net.minecraft.client.gui.screen.Screen {

    private final Screen parent;
    private final String previousName;
    private final String worldName;
    private final Consumer<String> consumer;
    private TextFieldWidget nameBox;

    public NameSaveScreen(Screen parent, String previousName, String worldName, Consumer<String> consumer) {
        super(Text.empty());
        this.parent = parent;
        this.previousName = previousName;
        this.worldName = worldName;
        this.consumer = consumer;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (getFocused() == nameBox && keyCode == 257 || keyCode == 335) {
            consumer.accept(nameBox.getText());
            return true;
        }

        return false;
    }

    @Override
    protected void init() {
        nameBox = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, null, Text.empty());
        addDrawableChild(nameBox);

        if (previousName != null && !previousName.equals(worldName))
            nameBox.setText(previousName);

        Text message;
        if (previousName == null || previousName.isEmpty())
            message = Text.translatable("savemod.name.create");
        else
            message = Text.translatable("savemod.name.rename");

        addDrawableChild(ButtonWidget.builder(message, button -> consumer.accept(nameBox.getText())
        ).dimensions(width / 2 - 150 - 5, height / 2 + 25, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close()
        ).dimensions(width / 2 + 5, height / 2 + 25, 150, 20).build());

        setInitialFocus(nameBox);
    }

    @Override
    public void tick() {
        nameBox.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
        if (previousName == null || previousName.isEmpty())
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("savemod.name.title.new"), width / 2, height / 2 - 45, 0xFFFFFF);
        else
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("savemod.name.title.rename"), width / 2, height / 2 - 45, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("savemod.name.hint", worldName), width / 2, height / 2 - 30, 0x808080);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

}
