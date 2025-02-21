package com.paxus.paxstore.api.tool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

public class Utils {

    /**
     * Unzips a given ZIP file into the same directory and returns the extracted files.
     *
     * @param zipFilePath Path to the ZIP file.
     * @throws IOException if an I/O error occurs.
     */
    public static void unzip(String zipFilePath) throws IOException {
        File zipFile = new File(zipFilePath);
        String destDir = zipFile.getParent(); // Extract to the same directory

        File destFolder = new File(destDir);
        if (!destFolder.exists()) destFolder.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    extractFile(zis, outFile);
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Extracts a file from the ZIP input stream.
     *
     * @param zis     The ZipInputStream.
     * @param outFile The file to extract.
     * @throws IOException if an I/O error occurs.
     */
    public static void extractFile(ZipInputStream zis, File outFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    /**
     * Processes the extracted files.
     *
     * @param files An array of extracted files.
     */
    public static void processFiles(File[] files) {
        if (files == null) return;
        for (File file : files) {
            System.out.println("Processing: " + file.getAbsolutePath());
            // Add file processing logic here
        }
    }

    /**
     * Deletes the extracted files after use.
     *
     * @param files An array of files to delete.
     */
    public static void deleteFiles(File[] files) {
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFiles(file.listFiles()); // Recursively delete directories
            }
            System.out.println("Deleting: " + file.getAbsolutePath());
            file.delete();
        }
    }

    /**
     * Recursively lists all files in a directory, and match file.
     * Use suffix to match files: .apk, .zip, .txt
     * @param folder root folder
     * @param suffix suffix
     * @return matched file paths
     */
    public static List<String> listAndMatchFile(File folder, String suffix) {
        List<String> res = new ArrayList<>();

        if (folder == null || !folder.exists()) {
            App.logger.debug("folder does not exist: " + (folder != null ? folder.getAbsolutePath() : "null"));
            return res;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    res.addAll(listAndMatchFile(file, suffix));
                } else if (file.getName().endsWith(suffix)) {
                    String what = "wrong";
                    // double check the file is correct: use file name or parent name
                    if (suffix.equals(".apk") && file.getParentFile().getName().equals("apk")) {
                        what = "apk";
                    } else if (suffix.equals(".zip") && file.getParentFile().getName().equals("paxstore templates")) {
                        what = "parameter";
                    } else if (suffix.equals("ReleaseNote.txt")) {
                        what = "release note";
                    }
                    App.logger.debug("found " + what + " file" + (what.equals("wrong") ? ", pass" : (": " + file.getAbsolutePath())));
                    if (!what.equals("wrong")) {
                        res.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return res;
    }

    /**
     * Read all content in a file to a string.
     * @param filePath file path
     * @return string
     * @throws IOException
     */
    public static String readFileToString(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        return String.join("\n", lines);
    }
}

