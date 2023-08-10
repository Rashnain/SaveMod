package com.github.rashnain.savemod.gui;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.gui.widget.SaveListEntry;
import com.github.rashnain.savemod.gui.widget.SaveListWidget;
import com.github.rashnain.savemod.util.ZipUtil;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.PathUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.ExecutionException;

public class SelectSaveScreen extends Screen {

    protected final Screen parent;
    private SaveListWidget saveList;
    private TextFieldWidget searchBox;
    private ButtonWidget loadButton;
    private ButtonWidget renameButton;
    private ButtonWidget duplicateButton;
    private ButtonWidget deleteButton;

    public SelectSaveScreen(Screen parent) {
        super(Text.translatable("savemod.list.title"));
        this.parent = parent;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (keyCode == 257 || keyCode == 32 || keyCode == 335) {
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load);
            return true;
        }

        return false;
    }

    @Override
    protected void init() {
        searchBox = new TextFieldWidget(textRenderer, width / 2 - 100, 22, 200, 20, null, Text.empty());
        searchBox.setChangedListener(search -> {
            saveList.setSearch(search);
            changeButtons(saveList.getSelectedOrNull() != null);
        });
        addDrawableChild(searchBox);

        saveList = new SaveListWidget(this, client, width, height, 48, height - 64, 36);
        addSelectableChild(saveList);

        loadButton = addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.list.play"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load)
        ).dimensions(width / 2 - 154, height - 52, 150, 20).build());
        loadButton.active = false;

        addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.list.create"), button ->
            client.setScreen(new NameSaveScreen(this, "", SaveMod.worldDir, this::save))
        ).dimensions(width / 2 + 4, height - 52, 150, 20).build());

        renameButton = addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.list.rename"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::rename)
        ).dimensions(width / 2 - 154, height - 28, 72, 20).build());
        renameButton.active = false;

        deleteButton = addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.list.delete"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::delete)
        ).dimensions(width / 2 - 76, height - 28, 72, 20).build());
        deleteButton.active = false;

        duplicateButton = addDrawableChild(ButtonWidget.builder(Text.translatable("savemod.list.duplicate"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::duplicate)
        ).dimensions(width / 2 + 4, height - 28, 72, 20).build());
        duplicateButton.active = false;

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close()
        ).dimensions(width / 2 + 82, height - 28, 72, 20).build());

        setInitialFocus(searchBox);
    }

    @Override
    public void tick() {
        searchBox.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        saveList.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 8, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    public void changeButtons(boolean buttonsActive) {
        loadButton.active = buttonsActive;
        renameButton.active = buttonsActive;
        duplicateButton.active = buttonsActive;
        deleteButton.active = buttonsActive;
    }

    public void save(String saveName) {
        client.setScreenAndRender(new MessageScreen(Text.translatable("savemod.message.saving")));
        if (client.isIntegratedServerRunning())
            client.getServer().saveAll(false, true, false);
        String worldDir = SaveMod.worldDir;
        try {
            DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4).appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_')
                .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

            String backupName = LocalDateTime.now().format(TIME_FORMATTER) + "_" + worldDir;
            if (!saveName.isEmpty())
                backupName = backupName.substring(0, 20) + saveName;

            Path savesDir = SaveMod.DIR.resolve(worldDir);
            if (Files.notExists(savesDir))
                Files.createDirectories(savesDir);

            Path backupFileName = savesDir.resolve(PathUtil.getNextUniqueName(savesDir, backupName, ".zip"));

            ZipUtil.createBackup("saves/" + worldDir, backupFileName.toString());

            client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.succesful"), Text.translatable("savemod.toast.succesful.save")));

            saveList.refresh();

            if (client.isIntegratedServerRunning()) {
                client.setScreen(null);
                return;
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.failed"), Text.translatable("savemod.toast.failed.save")));
            SaveMod.LOGGER.error("Could not save : {}", e.getMessage());
        }
        client.setScreen(this);
    }

}
