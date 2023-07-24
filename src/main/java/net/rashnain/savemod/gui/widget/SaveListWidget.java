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
import java.util.*;

public class SaveListWidget extends AlwaysSelectedEntryListWidget<SaveListEntry> {

    private final SelectSaveScreen parent;
    private List<SaveSummary> levels;
    private String search;

    public SaveListWidget(SelectSaveScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.parent = parent;
        search = "";
        refresh();
    }

    @Override
    public void setSelected(@Nullable SaveListEntry entry) {
        super.setSelected(entry);
        parent.changeButtons(entry != null);
    }

    @Override
    public boolean removeEntryWithoutScrolling(SaveListEntry entry) {
        return super.removeEntryWithoutScrolling(entry);
    }

    public Screen getParent() {
        return parent;
    }

    public void refresh() {
        showSummaries(search, getSaves());
    }

    private void showSummaries(String search, List<SaveSummary> summaries) {
        clearEntries();
        levels = summaries;
        for (SaveSummary summary : summaries) {
            if (!shouldShow(search, summary)) continue;
            addEntry(new SaveListEntry(summary, this));
        }
    }

    private List<SaveSummary> getSaves() {
        List<SaveSummary> saveList = new ArrayList<>();

        if (SaveMod.worldDir == null)
            return saveList;

        File savesDir = Path.of("savemod").resolve(SaveMod.worldDir).toFile();

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

        if (entry == null)
            return Optional.empty();

        return Optional.of(entry);
    }

    public void setSearch(String search) {
        if (!search.equals(this.search)) {
            showSummaries(search, levels);
            this.search = search;
        }
    }

    private boolean shouldShow(String search, SaveSummary summary) {
        return summary.getSaveName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

}
