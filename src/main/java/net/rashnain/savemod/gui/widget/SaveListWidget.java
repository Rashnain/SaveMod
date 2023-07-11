package net.rashnain.savemod.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.rashnain.savemod.SaveMod;
import net.rashnain.savemod.SaveSummary;
import net.rashnain.savemod.gui.SelectSaveScreen;

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
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        parent.changeButtons(getSelectedOrNull() != null);
        return result;
    }

    @Override
    public boolean removeEntryWithoutScrolling(SaveListEntry entry) {
        return super.removeEntryWithoutScrolling(entry);
    }

    public Screen getParent() {
        return parent;
    }

    public void refresh() {
        showSummaries(getSaves());
    }

    private void showSummaries(List<SaveSummary> summaries) {
        clearEntries();
        for (SaveSummary summary : summaries) {
            addEntry(new SaveListEntry(summary, this));
        }
    }

    private List<SaveSummary> getSaves() {
        List<SaveSummary> saveList = new ArrayList<>();

        if (SaveMod.worldDir == null)
            return saveList;

        File savesDir = new File(Path.of("savemod").resolve(SaveMod.worldDir).toUri());

        File[] tabFiles = savesDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".zip") && pathname.getName().length() > 24);

        if (tabFiles != null) {
            List<File> files = new ArrayList<>(List.of(tabFiles));
            Collections.reverse(files);

            for (File saveFile : files) {
                SaveSummary saveSummary = new SaveSummary(saveFile.getName(), SaveMod.worldDir);
                saveList.add(saveSummary);
            }
        }

        return saveList;
    }

    public Optional<SaveListEntry> getSelectedAsOptional() {
        SaveListEntry entry = getSelectedOrNull();
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

}
