package com.github.rashnain.savemod.gui;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.gui.widget.SaveListEntry;
import com.github.rashnain.savemod.gui.widget.SaveListWidget;
import com.github.rashnain.savemod.util.ZipUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SelectSaveScreen extends Screen {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 4).appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_')
        .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

    protected final Screen parent;
    protected final Runnable actionWhenClosed;
    private SaveListWidget saveList;
    private EditBox searchBox;
    private Button loadButton;
    private Button renameButton;
    private Button duplicateButton;
    private Button deleteButton;

    public SelectSaveScreen(Screen parent) {
        this(parent, null);
    }

    public SelectSaveScreen(Screen parent, Runnable actionWhenClosed) {
        super(Component.translatable("savemod.list.title"));
        this.parent = parent;
        this.actionWhenClosed = actionWhenClosed;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent))
            return true;

        if (keyEvent.isSelection()) {
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load);
            return true;
        }

        return false;
    }

    @Override
    protected void init() {
        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 20 + 4, 60);

        LinearLayout directionalLayoutWidget = layout.addToHeader(LinearLayout.vertical().spacing(4));
        directionalLayoutWidget.defaultCellSetting().alignHorizontallyCenter();
        directionalLayoutWidget.addChild(new StringWidget(title, font));
        LinearLayout directionalLayoutWidget2 = directionalLayoutWidget.addChild(LinearLayout.horizontal().spacing(4));

        searchBox = directionalLayoutWidget2.addChild(new EditBox(font, 0, 0, 200, 20, searchBox, Component.empty()));
        searchBox.setResponder(search -> {
            saveList.setSearch(search);
            changeButtons(saveList.getSelected() != null);
        });

        GridLayout gridWidget = layout.addToFooter((new GridLayout()).columnSpacing(8).rowSpacing(4));
        gridWidget.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(4);

        saveList = new SaveListWidget(this, minecraft, width, layout.getContentHeight(), layout.getHeaderHeight(), 36);
        saveList.setSearch(searchBox.getValue());
        layout.addToContents(saveList);

        loadButton = adder.addChild(Button.builder(Component.translatable("savemod.list.play"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load)
        ).build(), 2);
        loadButton.active = false;

        adder.addChild(Button.builder(Component.translatable("savemod.list.create"), button ->
            minecraft.setScreen(new NameSaveScreen(this, "", SaveMod.worldDir, this::save))
        ).build(), 2);

        renameButton = adder.addChild(Button.builder(Component.translatable("savemod.list.rename"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::rename)
        ).width(71).build());
        renameButton.active = false;

        deleteButton = adder.addChild(Button.builder(Component.translatable("savemod.list.delete"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::delete)
        ).width(71).build());
        deleteButton.active = false;

        duplicateButton = adder.addChild(Button.builder(Component.translatable("savemod.list.duplicate"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::duplicate)
        ).width(71).build());
        duplicateButton.active = false;

        adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()
        ).width(71).build());

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();

        setInitialFocus(searchBox);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
        if (actionWhenClosed != null)
            actionWhenClosed.run();
    }

    public void changeButtons(boolean buttonsActive) {
        loadButton.active = buttonsActive;
        renameButton.active = buttonsActive;
        duplicateButton.active = buttonsActive;
        deleteButton.active = buttonsActive;
    }

    public void save(String saveName) {
        ProgressScreen screen = new ProgressScreen(false);
        screen.progressStartNoAbort(Component.translatable("savemod.message.saving"));
        minecraft.setScreenAndShow(screen);

        if (minecraft.hasSingleplayerServer()) {
            IntegratedServer server = minecraft.getSingleplayerServer();
            CompletableFuture.runAsync(() -> server.saveEverything(false, true, false), server)
                .thenRunAsync(() -> finishSaving(saveName), minecraft)
                .thenRun(screen::stop);
        } else {
            finishSaving(saveName);
            screen.stop();
        }
    }

    private void finishSaving(String saveName) {
        String worldDir = SaveMod.worldDir;
        try {
            String backupName = LocalDateTime.now().format(TIME_FORMATTER) + "_" + worldDir;
            if (!saveName.isEmpty())
                backupName = backupName.substring(0, 20) + saveName;

            Path saveDir = SaveMod.DIR.resolve(worldDir);
            if (Files.notExists(saveDir))
                Files.createDirectories(saveDir);

            Path backupFileName = saveDir.resolve(backupName + ".zip");

            ZipUtil.createBackup("saves/" + worldDir, backupFileName.toString());

            minecraft.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.succesful"), Component.translatable("savemod.toast.succesful.save")));

            saveList.refresh();

            if (minecraft.hasSingleplayerServer()) {
                minecraft.setScreen(null);
                return;
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            minecraft.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.failed"), Component.translatable("savemod.toast.failed.save")));
            SaveMod.LOGGER.error("Could not save : {}", e.getMessage());
        }
        minecraft.setScreen(this);
    }

}
