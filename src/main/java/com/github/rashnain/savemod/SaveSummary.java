package com.github.rashnain.savemod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class SaveSummary {

    private final String saveFileName;
    private final String worldDir;
    private final long size;
    private long lastPlayed = -1;

    public SaveSummary(String saveFileName, String worldDir, long size) {
        this.saveFileName = saveFileName;
        this.worldDir = worldDir;
        this.size = size;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public String getSaveName() {
        return saveFileName.substring(20, saveFileName.length() - 4);
    }

    public String getWorldDir() {
        return worldDir;
    }

    public long getLastPlayed() {
        if (lastPlayed == -1) {
            Date date;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").parse(saveFileName);
            } catch (ParseException e) {
                SaveMod.LOGGER.error("Could not parse save date from '{}' : {}", saveFileName, e);
                date = Date.from(Instant.EPOCH);
            }
            lastPlayed = date.getTime();
        }
        return lastPlayed;
    }

    public String getSizeInMB() {
        long megaByte = 1000000;

        if (size < megaByte)
            return "< 1";

        return String.valueOf(size / megaByte);
    }

}
