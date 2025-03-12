package com.paxus.paxstore.api.tool;

import static com.pax.market.api.sdk.java.api.constant.Constants.APP_TYPE_NORMAL;
import static com.pax.market.api.sdk.java.api.constant.Constants.APP_TYPE_PARAMETER;
import static com.paxus.paxstore.api.tool.App.appName;
import static com.paxus.paxstore.api.tool.App.cfgFolderPath;
import static com.paxus.paxstore.api.tool.App.cfgJson;
import static com.paxus.paxstore.api.tool.App.logger;
import static com.paxus.paxstore.api.tool.App.pkgName;
import static com.paxus.paxstore.api.tool.App.releaseFolderPath;

import com.pax.market.api.sdk.java.api.developer.dto.CreateApkRequest;
import com.pax.market.api.sdk.java.api.developer.dto.step.CreateSingleApkRequest;
import com.pax.market.api.sdk.java.api.io.UploadedFileContent;
import com.pax.market.api.sdk.java.api.util.FileUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
     * Recursively lists all files in a directory, and match file.
     * Use suffix to match files: .apk, .zip, .txt
     *
     * @param folder root folder
     * @param suffix suffix
     * @return matched file paths
     */
    public static List<String> listAndMatchFile(File folder, String suffix) {
        List<String> res = new ArrayList<>();

        if (folder == null || !folder.exists()) {
            logger.debug("folder does not exist: " + (folder != null ? folder.getAbsolutePath() : "null"));
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
                    } else if (suffix.equals(".txt") && file.getName().startsWith("ReleaseNote")) {
                        what = "release note";
                    }
                    logger.debug("found " + what + " file" + (what.equals("wrong") ? ", pass" : (": " + file.getAbsolutePath())));
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
     *
     * @param filePath file path
     * @return string
     * @throws IOException
     */
    public static String loadFileToString(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        return String.join("\n", lines);
    }

    /**
     * Convert file path list to upload file content list used on paxstore api.
     *
     * @param filePaths a list of file path, String
     * @return a list of uploaded file content, UploadedFileContent
     */
    public static List<UploadedFileContent> createUploadFiles(List<String> filePaths) {
        return createUploadFiles("", filePaths);
    }

    /**
     * Convert file path list to upload file content list used on paxstore api.
     * add a variant prefix as the subfolder
     */
    public static List<UploadedFileContent> createUploadFiles(String variant, List<String> filePaths) {
        return filePaths.stream()
                .map(path -> variant + File.separator + path)
                .map(Utils::createUploadFile)
                .collect(Collectors.toList());
    }

    /**
     * FileUtils.createUploadFile and validate file path
     *
     * @param filePath file path
     * @return UploadedFileContent for a single file
     */
    public static UploadedFileContent createUploadFile(String filePath) {
        return createUploadFile("", filePath);
    }

    /**
     * FileUtils.createUploadFile and validate file path
     * add a variant prefix as the subfolder
     * @param filePath file path
     * @return UploadedFileContent for a single file
     */
    public static UploadedFileContent createUploadFile(String variant, String filePath) {
        filePath = variant + File.separator + filePath;
        if (!new File(filePath).exists()) {  // cfg only has file name
            filePath = cfgFolderPath + filePath;
        }
        if (!new File(filePath).exists()) {  // check again
            logger.error(filePath + " doesn't exist.");
            return null;
        }
        return FileUtils.createUploadFile(filePath);
    }

    /**
     * Create Apk request. Used for uploadApk
     *
     * @return CreateApkRequest
     */
    public static CreateApkRequest createApkRequest() throws IOException {
        // unzip release folder
        try {
            FileUtils.delFolder(releaseFolderPath);  // delete folder if exists
            Utils.unzip(App.releaseFolderPath + ".zip"); //  unzip
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }

        // get file path from release folder
        File releaseFolder = new File(releaseFolderPath);
        if (!releaseFolder.exists()) {
            logger.error(releaseFolderPath + " doesn't exist. Unzip release folder failed.");
            return null;
        }
        String apkSuffix = ".apk", releaseNoteSuffix = ".txt", paramSuffix = ".zip";

        List<String> apkFilePaths = Utils.listAndMatchFile(releaseFolder, apkSuffix);
        List<String> releaseNoteFilePaths = Utils.listAndMatchFile(releaseFolder, releaseNoteSuffix);
        List<String> paramFilePaths = Utils.listAndMatchFile(releaseFolder, paramSuffix);

        // apk file path, release note, param template list
        if (apkFilePaths.size() != 1) {
            logger.error("There should only be one apk file. Found: " + apkFilePaths);
            return null;
        }
        String apkFilePath = apkFilePaths.get(0);
        if (releaseNoteFilePaths.size() != 1) {
            logger.error("There should only be one release note file. Found: " + releaseNoteFilePaths);
            return null;
        }
        String releaseNote = loadFileToString(releaseNoteFilePaths.get(0));
        List<UploadedFileContent> paramTemplateList = Utils.createUploadFiles(paramFilePaths);

        // other info, read from cfg
        Config cfg = Config.loadJson(cfgFolderPath + cfgJson, pkgName);
        if (cfg == null) {
            logger.error("load " + cfgFolderPath + cfgJson + " failed, please check work directory or config fields.");
            logger.error("current work directory: " + new File("").getAbsolutePath());
            return null;
        }
        // TODO: more cfg validation
        if (paramFilePaths.size() == 0 && cfg.baseType.equals(APP_TYPE_PARAMETER)) {
            logger.error("Parameter app but no param file found.");
            return null;
        }

        logger.info("collected apk: " + apkFilePaths);
        logger.info("collected parameter templates: " + paramFilePaths);
        logger.info("collected release note: " + releaseNoteFilePaths);
        // create request
        CreateApkRequest createApkRequest = new CreateApkRequest();
        createApkRequest.setAppFile(Utils.createUploadFile(apkFilePath));
        createApkRequest.setAppName(appName);
        createApkRequest.setBaseType(cfg.baseType);
        createApkRequest.setShortDesc(cfg.shortDesc);
        createApkRequest.setDescription(cfg.fullDesc);
        createApkRequest.setReleaseNotes(releaseNote);
        createApkRequest.setChargeType(cfg.chargeType);
        createApkRequest.setCategoryList(cfg.categoryList);
        createApkRequest.setModelNameList(cfg.modelNameList);
        createApkRequest.setScreenshotFileList(Utils.createUploadFiles(cfg.variantName, cfg.screenshotFilePaths));
        createApkRequest.setParamTemplateFileList(paramTemplateList);
        createApkRequest.setFeaturedImgFile(Utils.createUploadFile(cfg.variantName, cfg.featureImgFilePath));
        createApkRequest.setIconFile(Utils.createUploadFile(cfg.variantName, cfg.iconFilePath));

        return createApkRequest;
    }

    /**
     * Create Single Apk request. Used for createApk
     * diff from createApkRequest: id, setApkName, setApkType
     *
     * @return CreateSingleApkRequest
     */
    public static CreateSingleApkRequest createSingleApkRequest(long id) throws IOException {
        // unzip release folder
        try {
            FileUtils.delFolder(releaseFolderPath);  // delete folder if exists
            Utils.unzip(App.releaseFolderPath + ".zip"); //  unzip
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }

        // get file path from release folder
        File releaseFolder = new File(releaseFolderPath);
        if (!releaseFolder.exists()) {
            logger.error(releaseFolderPath + " doesn't exist. Unzip release folder failed.");
            return null;
        }
        String apkSuffix = ".apk", releaseNoteSuffix = "ReleaseNote.txt", paramSuffix = ".zip";

        List<String> apkFilePaths = Utils.listAndMatchFile(releaseFolder, apkSuffix);
        List<String> releaseNoteFilePaths = Utils.listAndMatchFile(releaseFolder, releaseNoteSuffix);
        List<String> paramFilePaths = Utils.listAndMatchFile(releaseFolder, paramSuffix);

        // apk file path, release note, param template list
        if (apkFilePaths.size() != 1) {
            logger.error("There should only be one apk file. Found: " + apkFilePaths);
            return null;
        }
        String apkFilePath = apkFilePaths.get(0);
        if (releaseNoteFilePaths.size() != 1) {
            logger.error("There should only be one release note file. Found: " + releaseNoteFilePaths);
            return null;
        }
        String releaseNote = loadFileToString(releaseNoteFilePaths.get(0));
        String baseType = pkgName.contains("manager") ? APP_TYPE_NORMAL : APP_TYPE_PARAMETER;
        if (paramFilePaths.size() == 0 && baseType.equals(APP_TYPE_PARAMETER)) {
            logger.error("Parameter app but no param file found.");
            return null;
        }
        List<UploadedFileContent> paramTemplateList = Utils.createUploadFiles(paramFilePaths);

        // other info, read from cfg
        // TODO: cfg validation
        Config cfg = Config.loadJson(cfgFolderPath + cfgJson, pkgName);
        if (cfg == null) {
            logger.error("load " + cfgFolderPath + cfgJson + " failed, please check work directory or config fields.");
            logger.error("current work directory: " + new File("").getAbsolutePath());
            return null;
        }

        // create request
        CreateSingleApkRequest singleApkRequest = new CreateSingleApkRequest();
        singleApkRequest.setAppId(id);
        singleApkRequest.setAppFile(Utils.createUploadFile(apkFilePath));
        singleApkRequest.setApkName(appName);
        singleApkRequest.setApkType(baseType);
        singleApkRequest.setShortDesc(cfg.shortDesc);
        singleApkRequest.setDescription(cfg.fullDesc);
        singleApkRequest.setReleaseNotes(releaseNote);
        singleApkRequest.setChargeType(cfg.chargeType);
        singleApkRequest.setCategoryList(cfg.categoryList);
        singleApkRequest.setModelNameList(cfg.modelNameList);
        singleApkRequest.setScreenshotFileList(Utils.createUploadFiles(cfg.variantName, cfg.screenshotFilePaths));
        singleApkRequest.setParamTemplateFileList(paramTemplateList);
        singleApkRequest.setFeaturedImgFile(Utils.createUploadFile(cfg.variantName, cfg.featureImgFilePath));
        singleApkRequest.setIconFile(Utils.createUploadFile(cfg.variantName, cfg.iconFilePath));
        return singleApkRequest;
    }

    /**
     * Read user input
     * @param prompt prompt
     * @return input
     */
    public static String input(String prompt) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print(prompt);
            String input = reader.readLine();  // Read user input
            logger.info("You entered: " + input);
            return input;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    /**
     * Check if the parameter has the helper option.
     * @param args  args
     * @param helpOption  helper option
     * @return true if param is empty or param has the helper option, else false
     */
    public static boolean hasHelpOption(String[] args, Options options) throws ParseException {
        // TODO: fix helper message
        boolean hasHelp = false;
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (args.length == 0 || cmd.hasOption(options.getOption("h"))) {
            hasHelp = true;
        }
        return hasHelp;
    }
}