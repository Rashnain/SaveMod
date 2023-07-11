package net.rashnain.savemod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class SaveSummary {

    private final String saveFileName;
    private final String worldDir;

    public SaveSummary(String saveFileName, String worldDir) {
        this.saveFileName = saveFileName;
        this.worldDir = worldDir;
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

    public String getDetails() {
        return "";
    }

    public long getLastPlayed() {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").parse(saveFileName);
        } catch (ParseException e) {
            date = Date.from(Instant.EPOCH);
            SaveMod.LOGGER.error("Could not parse save date from '{}' : {}", saveFileName, e);
        }
        return date.getTime();
    }

}
