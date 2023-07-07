package net.rashnain.savemod.gui;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.rashnain.savemod.SaveMod;

public class NamingSaveScreen extends Screen {

    private final SelectSaveScreen parent;
    private TextFieldWidget nameBox;
    private final boolean modifying;
    private final String worldName;

    public NamingSaveScreen(SelectSaveScreen parent, boolean modifying, String worldName) {
        super(Text.of(""));
        this.parent = parent;
        this.modifying = modifying;
        this.worldName = worldName;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    protected void init() {
        nameBox = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, nameBox, Text.translatable("selectWorld.search")));

        addDrawableChild(ButtonWidget.builder(Text.of("Apply"), button -> {
                    parent.disconnectAndSave(nameBox.getText());
                    close();
                }
        ).dimensions(width / 2 - 150 - 5, height / 2 + 25, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), button ->
            close()
        ).dimensions(width / 2 + 5, height / 2 + 25, 150, 20).build());

        setInitialFocus(nameBox);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void tick() {
        this.nameBox.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return nameBox.keyPressed(keyCode, scanCode, modifiers);
    }

//    @Override
//    public boolean charTyped(char chr, int modifiers) {
//        return this.nameBox.charTyped(chr, modifiers);
//    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(matrices);
        if (modifying) {
            drawCenteredTextWithShadow(matrices, textRenderer, Text.of("Rename your save"), width / 2, height / 2 - 45, 16777215);
        } else {
            drawCenteredTextWithShadow(matrices, textRenderer, Text.of("Name your new save"), width / 2, height / 2 - 45, 16777215);
        }
        drawCenteredTextWithShadow(matrices, textRenderer, Text.of("Leave blank for \"" + worldName + "\""), width / 2, height / 2 - 30, 8421504);
        super.render(matrices, mouseX, mouseY, delta);
    }

}
