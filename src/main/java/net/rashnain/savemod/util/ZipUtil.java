package net.rashnain.savemod.util;

import net.rashnain.savemod.config.SaveModConfig;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    public static void unzipFile(String sourceFile, String targetDir) throws IOException {
        byte[] buffer = new byte[65536];
        int length;

        File previous = null;
        File current;

        try (ZipFile archive = new ZipFile(sourceFile)) {

            Enumeration<? extends ZipEntry> entries = archive.entries();

            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();

                File file = new File(targetDir, entry.getName());

                current = file.getParentFile();
                if (!current.equals(previous)) {
                    if (!current.exists())
                        current.mkdirs();
                    previous = current;
                }

                InputStream in = new BufferedInputStream(archive.getInputStream(entry), 65536);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 65536);

                while ((length = in.read(buffer)) != -1)
                    out.write(buffer, 0, length);

                out.close();
                in.close();
            }
        }
    }

    public static void createBackup(String worldDir, String targetFile) throws IOException, ExecutionException, InterruptedException {

        ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();

        try (ZipArchiveOutputStream archive = new ZipArchiveOutputStream(new FileOutputStream(targetFile))) {

            File sourceDir = new File(worldDir);

            Iterator<File> fileIterator = FileUtils.iterateFiles(sourceDir, null, true);

            archive.setUseZip64(Zip64Mode.AsNeeded);

            int sourceDirLength = sourceDir.getParentFile().getAbsolutePath().length() + 1;

            while (fileIterator.hasNext()) {

                File file = fileIterator.next();

                if (file.getName().equals("session.lock"))
                    continue;

                String relativePath = file.getAbsolutePath().substring(sourceDirLength);

                InputStreamSupplier streamSupplier = () -> {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        return InputStream.nullInputStream();
                    }
                };

                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(relativePath);

                if (SaveModConfig.compression.getValue())
                    zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                else
                    zipArchiveEntry.setMethod(ZipEntry.STORED);

                scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
            }
            scatterZipCreator.writeTo(archive);
        }
    }

}
