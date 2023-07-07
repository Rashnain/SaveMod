package net.rashnain.savemod.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.rashnain.savemod.SaveMod;
import net.rashnain.savemod.SaveSummary;
import net.rashnain.savemod.gui.SelectSaveScreen;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SaveListWidget extends AlwaysSelectedEntryListWidget<SaveListEntry> {

    private final SelectSaveScreen parent;

    public SaveListWidget(SelectSaveScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.parent = parent;
        refresh();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        parent.changeButtons(getSelectedOrNull() != null);

        return true;
    }

    private List<SaveSummary> getSaves() {
        if (SaveMod.worldDir == null)
            return null;

        File saveDir = new File(Path.of("savemod").resolve(SaveMod.worldDir).toUri());

        if (!saveDir.isDirectory())
            return null;

        List<SaveSummary> list = new ArrayList<>();

        List<File> files = new ArrayList<>(List.of(saveDir.listFiles()));
        Collections.reverse(files);
        for (File save : files) {
            SaveSummary saveSum = new SaveSummary(SaveMod.worldDir, save.getName());
            list.add(saveSum);
        }

        return list;
    }

    public Optional<SaveListEntry> getSelectedAsOptional() {
        SaveListEntry entry = getSelectedOrNull();
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    public void refresh() {
        show(getSaves());
    }

    @Override
    public boolean removeEntryWithoutScrolling(SaveListEntry entry) {
        boolean result = super.removeEntryWithoutScrolling(entry);
        if (getSelectedOrNull() == null)
            parent.changeButtons(false);
        return result;
    }

    private void show(@Nullable List<SaveSummary> saves) {
        if (saves != null) {
            showSummaries(saves);
        }
    }

    private void showSummaries(List<SaveSummary> summaries) {
        clearEntries();
        for (SaveSummary summary : summaries) {
            addEntry(new SaveListEntry(summary, this));
        }
    }

}
