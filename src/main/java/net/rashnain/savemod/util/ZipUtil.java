package net.rashnain.savemod.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    public static void unzipFile(String zipFile, String targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path path = Path.of(targetDir).resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    if (Files.notExists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

}
