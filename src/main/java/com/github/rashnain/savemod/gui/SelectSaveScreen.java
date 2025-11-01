package com.github.rashnain.savemod.gui;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.gui.widget.SaveListEntry;
import com.github.rashnain.savemod.gui.widget.SaveListWidget;
import com.github.rashnain.savemod.util.ZipUtil;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.path.PathUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.ExecutionException;

public class SelectSaveScreen extends Screen {

    private ThreePartsLayoutWidget layout;
    protected final Screen parent;
    protected final Runnable actionWhenClosed;
    private SaveListWidget saveList;
    private TextFieldWidget searchBox;
    private ButtonWidget loadButton;
    private ButtonWidget renameButton;
    private ButtonWidget duplicateButton;
    private ButtonWidget deleteButton;

    public SelectSaveScreen(Screen parent) {
        this(parent, null);
    }

    public SelectSaveScreen(Screen parent, Runnable actionWhenClosed) {
        super(Text.translatable("savemod.list.title"));
        this.parent = parent;
        this.actionWhenClosed = actionWhenClosed;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input))
            return true;

        if (input.isEnterOrSpace()) {
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load);
            return true;
        }

        return false;
    }

    @Override
    protected void init() {
        layout = new ThreePartsLayoutWidget(this, 8 + 9 + 8 + 20 + 4, 60);

        DirectionalLayoutWidget directionalLayoutWidget = layout.addHeader(DirectionalLayoutWidget.vertical().spacing(4));
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
        directionalLayoutWidget.add(new TextWidget(title, textRenderer));
        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(DirectionalLayoutWidget.horizontal().spacing(4));

        searchBox = directionalLayoutWidget2.add(new TextFieldWidget(textRenderer, 200, 20, Text.empty()));
        searchBox.setChangedListener(search -> {
            saveList.setSearch(search);
            changeButtons(saveList.getSelectedOrNull() != null);
        });

        GridWidget gridWidget = layout.addFooter((new GridWidget()).setColumnSpacing(8).setRowSpacing(4));
        gridWidget.getMainPositioner().alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(4);

        saveList = new SaveListWidget(this, client, width, layout.getContentHeight(), layout.getHeaderHeight(), 36);
        layout.addBody(saveList);

        loadButton = adder.add(ButtonWidget.builder(Text.translatable("savemod.list.play"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::load)
        ).build(), 2);
        loadButton.active = false;

        adder.add(ButtonWidget.builder(Text.translatable("savemod.list.create"), button ->
            client.setScreen(new NameSaveScreen(this, "", SaveMod.worldDir, this::save))
        ).build(), 2);

        renameButton = adder.add(ButtonWidget.builder(Text.translatable("savemod.list.rename"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::rename)
        ).width(71).build());
        renameButton.active = false;

        deleteButton = adder.add(ButtonWidget.builder(Text.translatable("savemod.list.delete"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::delete)
        ).width(71).build());
        deleteButton.active = false;

        duplicateButton = adder.add(ButtonWidget.builder(Text.translatable("savemod.list.duplicate"), button ->
            saveList.getSelectedAsOptional().ifPresent(SaveListEntry::duplicate)
        ).width(71).build());
        duplicateButton.active = false;

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> close()
        ).width(71).build());

        layout.forEachChild(this::addDrawableChild);
        layout.refreshPositions();

        setInitialFocus(searchBox);
    }

    @Override
    public void close() {
        client.setScreen(parent);
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

            Path saveDir = SaveMod.DIR.resolve(worldDir);
            if (Files.notExists(saveDir))
                Files.createDirectories(saveDir);

            Path backupFileName = saveDir.resolve(PathUtil.getNextUniqueName(saveDir, backupName, ".zip"));

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
