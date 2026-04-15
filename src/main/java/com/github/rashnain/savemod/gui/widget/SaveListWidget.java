package com.github.rashnain.savemod.gui.widget;

import com.github.rashnain.savemod.SaveMod;
import com.github.rashnain.savemod.SaveSummary;
import com.github.rashnain.savemod.gui.SelectSaveScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class SaveListWidget extends ObjectSelectionList<SaveListEntry> {

    private final SelectSaveScreen parent;
    private List<SaveSummary> saves;
    private String search;

    public SaveListWidget(SelectSaveScreen parent, Minecraft client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);
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
    public void removeEntryFromTop(SaveListEntry entry) {
        super.removeEntryFromTop(entry);
    }

    @Override
    public int getRowWidth() {
        return 270;
    }

    public Screen getParent() {
        return parent;
    }

    public void refresh() {
        showSummaries(search, getSaves());
    }

    private void showSummaries(String search, List<SaveSummary> summaries) {
        clearEntries();
        saves = summaries;
        for (SaveSummary summary : summaries) {
            if (!shouldShow(search, summary)) continue;
            addEntry(new SaveListEntry(summary, this));
        }
    }

    private List<SaveSummary> getSaves() {
        List<SaveSummary> saveList = new ArrayList<>();

        if (SaveMod.worldDir == null)
            return saveList;

        File saveDir = SaveMod.DIR.resolve(SaveMod.worldDir).toFile();

        File[] files = saveDir.listFiles(file -> file.isFile() && file.getName().endsWith(".zip") && file.getName().length() > 24);

        if (files != null) {
            Arrays.sort(files, Collections.reverseOrder());

            for (File saveFile : files) {
                SaveSummary saveSummary = new SaveSummary(saveFile.getName(), SaveMod.worldDir, saveFile.length());
                saveList.add(saveSummary);
            }
        }

        return saveList;
    }

    public Optional<SaveListEntry> getSelectedAsOptional() {
        SaveListEntry entry = getSelected();
        if (entry == null)
            return Optional.empty();
        return Optional.of(entry);
    }

    public void setSearch(String search) {
        if (!search.equals(this.search)) {
            showSummaries(search, saves);
            this.search = search;
        }
    }

    private boolean shouldShow(String search, SaveSummary summary) {
        return summary.getSaveName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

}
