package com.github.rashnain.savemod.gui.widget;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.SaveSummary;
import com.github.rashnain.savemod.gui.NameSaveScreen;
import com.github.rashnain.savemod.util.ZipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveListEntry extends ObjectSelectionList.Entry<SaveListEntry> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final Identifier UNKNOWN_SERVER_LOCATION = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");
    public static final Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("world_list/join_highlighted");
    public static final Identifier JOIN_TEXTURE = Identifier.withDefaultNamespace("world_list/join");

    private final Minecraft client;
    private final SaveListWidget saveList;
    private final SaveSummary save;
    private final Path saveDir;
    private final Path saveFile;
    private long time;

    public SaveListEntry(SaveSummary save, SaveListWidget parent) {
        this.save = save;
        saveList = parent;
        client = Minecraft.getInstance();
        saveDir = SaveMod.DIR.resolve(save.getWorldDir());
        saveFile = saveDir.resolve(save.getSaveFileName());
    }

    @Override
    public Component getNarration() {
        return Component.nullToEmpty(save.getSaveName());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.x() - saveList.getRowLeft() <= 32.0) {
            load();
            return true;
        }

        if (Util.getMillis() - time < 250L) {
            load();
            return true;
        }
        time = Util.getMillis();

        return true;
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
        int x = this.getContentX();
        int y = this.getContentY();
        String displayName = save.getSaveName();
        String folderNameAndLastPlayedDate = save.getWorldDir() + " (" + DATE_FORMAT.format(new Date(save.getLastPlayed())) + ")";
        String fileSize = save.getSizeInMB() + " MB";

        graphics.text(client.font, displayName, x + 32 + 3, y + 1, -1);
        graphics.text(client.font, folderNameAndLastPlayedDate, x + 32 + 3, y + 1 + client.font.lineHeight + 2, -0x808080);
        graphics.text(client.font, fileSize, x + 32 + 3, y + 1 + client.font.lineHeight * 2 + 2, -0x808080);

        graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_SERVER_LOCATION, x, y, 0.0f, 0.0f, 32, 32, 32, 32);

        if (hovered) {
            graphics.fill(x, y, x + 32, y + 32, -0x5F6F6F70);
            int pixelsBeforeStartButton = mouseX - x;
            Identifier texture = pixelsBeforeStartButton <= 32 ? JOIN_HIGHLIGHTED_TEXTURE : JOIN_TEXTURE;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, 32, 32);
        }
    }

    public void load() {
        if (client.hasSingleplayerServer())
            client.disconnectFromWorld(Component.translatable("savemod.message.closing"));
        client.setScreenAndShow(new GenericMessageScreen(Component.translatable("savemod.message.deleting")));
        String worldDir = save.getWorldDir();

        try {
            FileUtils.deleteDirectory(Path.of("saves").resolve(worldDir).toFile());
            client.setScreenAndShow(new GenericMessageScreen(Component.translatable("savemod.message.uncompressing")));
            String zipFile = saveDir.resolve(save.getSaveFileName()).toString();
            try {
                ZipUtil.unzipFile(zipFile, "saves/");
                client.setScreenAndShow(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
                client.createWorldOpenFlows().openWorld(worldDir, () -> {});
            } catch (IOException e) {
                client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.failed"), Component.translatable("savemod.toast.failed.uncompress")));
                SaveMod.LOGGER.error("Could not extract file '{}' : {}", zipFile, e);
                client.gui.setScreen(saveList.getParent());
            }
        } catch (IOException e) {
            client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.failed"), Component.translatable("savemod.toast.failed.load")));
            SaveMod.LOGGER.error("Could not delete world '{}' : {}", worldDir, e);
            client.gui.setScreen(saveList.getParent());
        }
    }

    public void rename() {
        client.gui.setScreen(new NameSaveScreen(saveList.getParent(), save.getSaveName(), saveDir.getFileName().toString(), newName -> {
            String saveFileName = saveFile.getFileName().toString();
            if (newName.isEmpty())
                newName = save.getWorldDir();
            saveFileName = saveFileName.substring(0, 20) + newName + ".zip";
            try {
                Files.move(saveFile, saveDir.resolve(saveFileName));
            } catch (IOException e) {
                client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.failed"), Component.translatable("savemod.toast.failed.name")));
                SaveMod.LOGGER.error("Could not rename save '{}' : {}", saveFile, e);
            }
            client.gui.setScreen(saveList.getParent());
        }));
    }

    public void duplicate() {
        String newSaveName = save.getSaveFileName().replaceFirst(".zip$", " " + Component.translatable("savemod.name.copy").getString() + ".zip");
        try {
            Files.copy(saveFile, saveDir.resolve(newSaveName));
            saveList.refresh();
            client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.succesful"), Component.translatable("savemod.toast.succesful.duplicate")));
        } catch (IOException e) {
            client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.failed"), Component.translatable("savemod.toast.failed.duplicate")));
            SaveMod.LOGGER.error("Could not duplicate save '{}' : {}", saveFile, e);
        }
    }

    public void delete() {
        client.gui.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    Files.delete(saveFile);
                    try {
                        Files.delete(saveDir);
                        Files.delete(SaveMod.DIR);
                    } catch (IOException ignored) {}
                    saveList.removeEntryFromTop(this);
                } catch (IOException e) {
                    client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("savemod.toast.failed"), Component.translatable("savemod.toast.failed.delete")));
                    SaveMod.LOGGER.error("Could not delete save '{}' : {}", saveFile, e);
                }
            }
            client.gui.setScreen(saveList.getParent());
        }, Component.translatable("savemod.delete.question"), Component.translatable("selectWorld.deleteWarning", save.getSaveName())));
    }

}
