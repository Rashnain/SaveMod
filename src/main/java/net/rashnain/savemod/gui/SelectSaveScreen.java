package net.rashnain.savemod.gui;

import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import net.rashnain.savemod.SaveMod;
import net.rashnain.savemod.gui.widget.SaveListEntry;
import net.rashnain.savemod.gui.widget.SaveListWidget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SelectSaveScreen extends Screen {

    protected final Screen parent;
    private SaveListWidget saveList;
    private ButtonWidget loadButton;
    private ButtonWidget duplicateButton;
    private ButtonWidget deleteButton;
    private ButtonWidget createButton;

    public SelectSaveScreen(Screen parent) {
        super(Text.of("Save list"));
        this.parent = parent;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY > this.height - 55) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return saveList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return saveList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return saveList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (saveList.isMouseOver(mouseX, mouseY)) {
            return saveList.mouseScrolled(mouseX, mouseY, amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    protected void init() {
        saveList = new SaveListWidget(this, client, width, height, 30, height - 64, 36);

        loadButton = addDrawableChild(ButtonWidget.builder(Text.of("Load"), button ->
                saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load)
        ).dimensions(width / 2 - 100 - 5 - 50, height - 52, 100, 20).build());
        loadButton.active = false;

        duplicateButton = addDrawableChild(ButtonWidget.builder(Text.of("Duplicate"), button ->
                saveList.getSelectedAsOptional().ifPresent(SaveListEntry::duplicate)
        ).dimensions(width / 2 - 50, height - 52, 100, 20).build());
        duplicateButton.active = false;

        deleteButton = addDrawableChild(ButtonWidget.builder(Text.of("Delete"), button ->
                saveList.getSelectedAsOptional().ifPresent(SaveListEntry::delete)
        ).dimensions(width / 2 + 50 + 5, height - 52, 100, 20).build());
        deleteButton.active = false;

        createButton = addDrawableChild(ButtonWidget.builder(Text.of("Create New Save"), button ->
                client.getAbuseReportContext().tryShowDraftScreen(client, null, this::disconnectAndSave, true)
        ).dimensions(width / 2 - 152 - 3, height - 28, 152, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.of("Done"), button ->
                close()
        ).dimensions(width / 2 + 3, height - 28, 152, 20).build());
    }

    private void disconnectAndSave() {
        if (client.isIntegratedServerRunning()) {
            client.world.disconnect();
            client.disconnect(new MessageScreen(Text.of("Closing world and saving...")));
            client.setScreen(this);
        }
        String worldDir = SaveMod.worldDir;
        try {
            LevelStorage.Session session = client.getLevelStorage().createSession(worldDir);
            EditWorldScreen.backupLevel(session);
            session.close();
            String backupName = SaveMod.backupName;
            Path backupsDir = Path.of("backups");
            Path saveDir = Path.of("savemod").resolve(worldDir);
            if (Files.notExists(saveDir)) {
                Files.createDirectories(saveDir);
            }
            Files.move(backupsDir.resolve(backupName), saveDir.resolve(backupName), StandardCopyOption.REPLACE_EXISTING);
            saveList.refresh();
            changeButtons(false);
            client.getToastManager().clear();
            client.getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, Text.of("Successful!"), Text.of("Save created.")));
        } catch (IOException e) {
            SystemToast.addWorldDeleteFailureToast(client, worldDir);
            SaveMod.LOGGER.error("Could not delete world '{}' : {}", worldDir, e);
        }
    }

    @Override
    public void close() {
        if (parent instanceof GameMenuScreen && !client.isIntegratedServerRunning()) {
            client.setScreen(new TitleScreen());
        } else {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        saveList.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 10, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void changeButtons(boolean buttonsActive) {
        loadButton.active = buttonsActive;
        duplicateButton.active = buttonsActive;
        deleteButton.active = buttonsActive;
        loadButton.setFocused(false);
        duplicateButton.setFocused(false);
        deleteButton.setFocused(false);
        createButton.setFocused(false);
    }

}
