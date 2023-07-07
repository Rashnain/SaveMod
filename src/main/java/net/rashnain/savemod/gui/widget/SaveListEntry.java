package net.rashnain.savemod.gui.widget;

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
import net.rashnain.savemod.SaveMod;
import net.rashnain.savemod.SaveSummary;
import net.rashnain.savemod.util.ZipUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

public class SaveListEntry extends AlwaysSelectedEntryListWidget.Entry<SaveListEntry> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
    private static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
    Identifier icon;
    Identifier iconLocation;

    private final MinecraftClient client;
    private final SaveSummary save;
    private final Path saveDir;
    private final Path savePath;
    private final SaveListWidget saveList;
    private long time;

    public SaveListEntry(SaveSummary save, SaveListWidget saveList) {
        this.save = save;
        client = MinecraftClient.getInstance();
        saveDir = Path.of("savemod").resolve(save.getName());
        savePath = saveDir.resolve(save.getSaveFileName());
        this.saveList = saveList;

    }

    @Override
    public Text getNarration() {
        return Text.of(save.getDisplayName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int delta) {
        if (mouseX - saveList.getRowLeft() <= 32.0) {
            this.load();
            return true;
        }
        if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.load();
            return true;
        }
        this.time = Util.getMeasuringTimeMs();

        return true;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        String displayName = save.getDisplayName();
        String folderNameAndLastPlayedDate = save.getName() + " (" + DATE_FORMAT.format(new Date(this.save.getLastPlayed())) + ")";
        String details = save.getDetails();
        client.textRenderer.draw(matrices, displayName, x + 32 + 3, y + 1, 0xFFFFFF);
        client.textRenderer.draw(matrices, folderNameAndLastPlayedDate, x + 32 + 3, y + client.textRenderer.fontHeight + 3, 0x808080);
        client.textRenderer.draw(matrices, details, x + 32 + 3, y + client.textRenderer.fontHeight * 2 + 3, 0x808080);
        RenderSystem.setShaderTexture(0, icon != null ? iconLocation : UNKNOWN_SERVER_LOCATION);
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        if (client.options.getTouchscreen().getValue() || hovered) {
            RenderSystem.setShaderTexture(0, WORLD_SELECTION_LOCATION);
            DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
            int pixelsBeforeStartButton = mouseX - x;
            int textureY = pixelsBeforeStartButton <= 32 ? 32 : 0;
            DrawableHelper.drawTexture(matrices, x, y, 0.0f, textureY, 32, 32, 256, 256);
        }
    }

    public void load() {
        if (client.isIntegratedServerRunning()) {
            client.world.disconnect();
            client.disconnect(new MessageScreen(Text.of("Closing and deleting previous world...")));
        }
        String directory = save.getName();
        try (LevelStorage.Session session = client.getLevelStorage().createSession(directory)) {
            session.deleteSessionLock();
            String zipFile = saveDir.resolve(save.getSaveFileName()).toString();
            try {
                ZipUtil.unzipFile(zipFile, "saves/");
                client.createIntegratedServerLoader().start(null, directory);
            } catch (IOException e) {
                SystemToast.addWorldAccessFailureToast(client, zipFile);
                SaveMod.LOGGER.error("Could not extract '{}' : {}", zipFile, e);
            }
        } catch (IOException e) {
            SystemToast.addWorldDeleteFailureToast(client, directory);
            SaveMod.LOGGER.error("Failed to delete world '{}' : {}", directory, e);
        }
    }

    public void duplicate() {
        Path file = Path.of(save.getSaveFileName().replaceFirst(".zip$", " Copy.zip"));
        try {
            if (!Files.exists(saveDir.resolve(file.toString()))) {
                Files.copy(savePath, saveDir.resolve(file.toString()), StandardCopyOption.REPLACE_EXISTING);
                saveList.refresh();
                saveList.removeEntryWithoutScrolling(null);
                client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Successful!"), Text.of("Save duplicated.")));
            } else {
                client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Failed!"), Text.of("File already exists.")));
            }
        } catch (IOException e) {
            client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Failed!"), Text.of("Save not duplicated.")));
            SaveMod.LOGGER.error("Failed to duplicate save '{}' : {}", savePath, e);
        }
    }

    public void delete() {
        client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    Files.delete(savePath);
                    try (Stream<Path> entries = Files.list(saveDir)) {
                        if (entries.findFirst().isEmpty())
                            Files.delete(saveDir);
                    }
                    saveList.removeEntryWithoutScrolling(this);
                    client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Successful!"), Text.of("Save deleted.")));
                } catch (IOException e) {
                    client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.of("Failed!"), Text.of("Save not deleted.")));
                    SaveMod.LOGGER.error("Failed to delete save '{}' : {}", savePath, e);
                }
            }
            client.setScreen(saveList.getParent());
        }, Text.of("Are you sure you want to delete this save ?"), Text.of("\"" + save.getDisplayName() + "\" will be lost forever ! (A long time !)")));
    }

}
