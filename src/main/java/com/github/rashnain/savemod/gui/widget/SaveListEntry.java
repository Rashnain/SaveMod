package com.github.rashnain.savemod.gui.widget;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.SaveSummary;
import com.github.rashnain.savemod.gui.NameSaveScreen;
import com.github.rashnain.savemod.util.ZipUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveListEntry extends AlwaysSelectedEntryListWidget.Entry<SaveListEntry> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
    private static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");

    private final MinecraftClient client;
    private final SaveListWidget saveList;
    private final SaveSummary save;
    private final Path savesDir;
    private final Path saveFile;
    private long time;

    public SaveListEntry(SaveSummary save, SaveListWidget parent) {
        this.save = save;
        saveList = parent;
        client = MinecraftClient.getInstance();
        savesDir = SaveMod.DIR.resolve(save.getWorldDir());
        saveFile = savesDir.resolve(save.getSaveFileName());
    }

    @Override
    public Text getNarration() {
        return Text.of(save.getSaveName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int delta) {
        if (mouseX - saveList.getRowLeft() <= 32.0) {
            load();
            return true;
        }

        if (Util.getMeasuringTimeMs() - time < 250L) {
            load();
            return true;
        }
        time = Util.getMeasuringTimeMs();

        return true;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        String displayName = save.getSaveName();
        String folderNameAndLastPlayedDate = save.getWorldDir() + " (" + DATE_FORMAT.format(new Date(save.getLastPlayed())) + ")";
        String fileSize = save.getSizeInMB() + " MB";

        client.textRenderer.draw(matrices, displayName, x + 32 + 3, y + 1, 0xFFFFFF);
        client.textRenderer.draw(matrices, folderNameAndLastPlayedDate, x + 32 + 3, y + 1 + 2 + client.textRenderer.fontHeight, 0x808080);
        client.textRenderer.draw(matrices, fileSize, x + 32 + 3, y + 1 + (2 + client.textRenderer.fontHeight) * 2, 0x808080);

        RenderSystem.setShaderTexture(0, UNKNOWN_SERVER_LOCATION);
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();

        if (client.options.getTouchscreen().getValue() || hovered) {
            RenderSystem.setShaderTexture(0, WORLD_SELECTION_LOCATION);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -0x5F6F6F70);
            int pixelsBeforeStartButton = mouseX - x;
            int textureY = pixelsBeforeStartButton <= 32 ? 32 : 0;
            DrawableHelper.drawTexture(matrices, x, y, 0.0f, textureY, 32, 32, 256, 256);
        }
    }

    public void load() {
        if (client.isIntegratedServerRunning()) {
            client.world.disconnect();
            client.disconnect(new MessageScreen(Text.translatable("savemod.message.closing")));
        }
        client.setScreenAndRender(new MessageScreen(Text.translatable("savemod.message.deleting")));
        String worldDir = save.getWorldDir();

        try (LevelStorage.Session session = client.getLevelStorage().createSession(worldDir)) {
            session.deleteSessionLock();
            client.setScreenAndRender(new MessageScreen(Text.translatable("savemod.message.uncompressing")));
            String zipFile = savesDir.resolve(save.getSaveFileName()).toString();
            try {
                ZipUtil.unzipFile(zipFile, "saves/");
                client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
                client.createIntegratedServerLoader().start(saveList.getParent(), worldDir);
            } catch (IOException e) {
                client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.failed"), Text.translatable("savemod.toast.failed.uncompress")));
                SaveMod.LOGGER.error("Could not extract file '{}' : {}", zipFile, e);
                client.setScreen(saveList.getParent());
            }
        } catch (IOException e) {
            client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.failed"), Text.translatable("savemod.toast.failed.load")));
            SaveMod.LOGGER.error("Could not delete world '{}' : {}", worldDir, e);
            client.setScreen(saveList.getParent());
        }
    }

    public void rename() {
        client.setScreen(new NameSaveScreen(saveList.getParent(), save.getSaveName(), savesDir.getFileName().toString(), newName -> {
            String saveFileName = saveFile.getFileName().toString();
            if (newName.isEmpty())
                newName = save.getWorldDir();
            saveFileName = saveFileName.substring(0, 20) + newName + ".zip";
            try {
                Files.move(saveFile, savesDir.resolve(saveFileName));
            } catch (IOException e) {
                client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.failed"), Text.translatable("savemod.toast.failed.name")));
                SaveMod.LOGGER.error("Could not rename save '{}' : {}", saveFile, e);
            }
            client.setScreen(saveList.getParent());
        }));
    }

    public void duplicate() {
        String newSaveName = save.getSaveFileName().replaceFirst(".zip$", " " + Text.translatable("savemod.name.copy").getString() + ".zip");
        try {
            Files.copy(saveFile, savesDir.resolve(newSaveName));
            saveList.refresh();
            client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.succesful"), Text.translatable("savemod.toast.succesful.duplicate")));
        } catch (IOException e) {
            client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.failed"), Text.translatable("savemod.toast.failed.duplicate")));
            SaveMod.LOGGER.error("Could not duplicate save '{}' : {}", saveFile, e);
        }
    }

    public void delete() {
        client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    Files.delete(saveFile);
                    try {
                        Files.delete(savesDir);
                        Files.delete(SaveMod.DIR);
                    } catch (IOException ignored) {}
                    saveList.removeEntryWithoutScrolling(this);
                } catch (IOException e) {
                    client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("savemod.toast.failed"), Text.translatable("savemod.toast.failed.delete")));
                    SaveMod.LOGGER.error("Could not delete save '{}' : {}", saveFile, e);
                }
            }
            client.setScreen(saveList.getParent());
        }, Text.translatable("savemod.delete.question"), Text.translatable("selectWorld.deleteWarning", save.getSaveName())));
    }

}
