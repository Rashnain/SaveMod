package net.rashnain.savemod.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    public static void unzipFile(String zipFile, String targetDir) throws IOException {
        byte[] buffer = new byte[65536];
        int length;
        File previous = null;
        File current;
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(targetDir, entry.getName());
                current = file.getParentFile();
                if (!current.equals(previous)) {
                    if (!current.exists())
                        current.mkdirs();
                    previous = current;
                }
                InputStream in = new BufferedInputStream(zip.getInputStream(entry), 65536);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 65536);
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
                out.close();
                in.close();
            }
        }
    }

}
