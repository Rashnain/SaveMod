package net.rashnain.savemod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveSummary {

    private final String fileName;
    private final String worldDir;

    public SaveSummary(String worldDir, String backupFileName) {
        this.worldDir = worldDir;
        fileName = backupFileName;
    }

    public String getDisplayName() {
        return fileName.substring(20, fileName.length() - 4);
    }

    public String getName() {
        return worldDir;
    }

    public String getDetails() {
        return "";
    }

    public long getLastPlayed() {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").parse(fileName);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date.getTime();
    }

    public String getSaveFileName() {
        return fileName;
    }

}
