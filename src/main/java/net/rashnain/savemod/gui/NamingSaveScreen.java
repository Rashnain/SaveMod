package net.rashnain.savemod.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NamingSaveScreen extends net.minecraft.client.gui.screen.Screen {

    private final Screen parent;
    private final String previousName;
    private final String worldName;
    private final PressAction pressAction;
    private TextFieldWidget nameBox;

    public NamingSaveScreen(Screen parent, String previousName, String worldName, PressAction pressAction) {
        super(Text.of(""));
        this.parent = parent;
        this.previousName = previousName;
        this.worldName = worldName;
        this.pressAction = pressAction;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            pressAction.onPress(nameBox.getText());
            return true;
        }
        return nameBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        nameBox = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, nameBox, Text.translatable("selectWorld.search")));

        if (previousName != null && !previousName.equals(worldName))
            nameBox.setText(previousName);

        addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.name.apply"), button ->
            pressAction.onPress(nameBox.getText())
        ).dimensions(width / 2 - 150 - 5, height / 2 + 25, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.name.cancel"), button ->
            close()
        ).dimensions(width / 2 + 5, height / 2 + 25, 150, 20).build());

        setInitialFocus(nameBox);
    }

    @Override
    public void tick() {
        nameBox.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(matrices);
        if (previousName != null && !previousName.isEmpty()) {
            drawCenteredTextWithShadow(matrices, textRenderer, Text.translatable("savemod.name.rename"), width / 2, height / 2 - 45, 16777215);
        } else {
            drawCenteredTextWithShadow(matrices, textRenderer, Text.translatable("savemod.name.new"), width / 2, height / 2 - 45, 16777215);
        }
        drawCenteredTextWithShadow(matrices, textRenderer, Text.translatable("savemod.name.hint", worldName), width / 2, height / 2 - 30, 8421504);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    public interface PressAction {
        void onPress(String var1);
    }

}
