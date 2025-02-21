package com.paxus.paxstore.api.tool;

import static com.pax.market.api.sdk.java.api.constant.Constants.APP_TYPE_NORMAL;
import static com.pax.market.api.sdk.java.api.constant.Constants.APP_TYPE_PARAMETER;

import com.pax.market.api.sdk.java.api.base.dto.AppDetailDTO;
import com.pax.market.api.sdk.java.api.base.dto.Result;
import com.pax.market.api.sdk.java.api.developer.DeveloperApi;
import com.pax.market.api.sdk.java.api.developer.dto.CreateApkRequest;
import com.pax.market.api.sdk.java.api.io.UploadedFileContent;
import com.pax.market.api.sdk.java.api.util.FileUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static final Logger logger = LoggerFactory.getLogger(App.class);
    public static final String releaseFolderPath = "release-folder.zip";
    public static final String cfgFolderPath = "cfg";
    public static final String[] commands = new String[]{"main", "getAppInfo", "uploadApk"};

    public static String apiKey, apiSecret, apiUrl, appName, pkgName, command;
    static DeveloperApi developerApi;

    public static void main(String[] args) {
        /**
         * arguments:
         * [--key] api key: github secret
         * [--secret] api secret: github secret
         * [--url] url: global paxstore, github secret?
         * [--app] app name:
         * [--package] package name:
         * required configs when upload:
         * [paths]
         * - apk
         * - release note
         * - param template
         * optional configs when upload:
         * [paths]
         * - screenshot
         * - feature image
         * - app icon
         * [string]
         * - basetype
         * - short desc
         * - desc
         * - charge type
         * - category list
         * - model name list
         */

        /** workflow
         * 1. getAppInfoByName(packageName, appName)
         * 2.1. businessCode = 0: has app, need only required configs
         * 2.2. businessCode != 0: new app, need all required and optional
         * 3. If any config missed, don't upload, failed
         *    else, uploadApk(createApkRequest), return the upload result
         */

        // print log to stdout
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");

        // options
        Options options = new Options();
        Option keyOption = createOption("k", "key", "API_KEY", "paxstore developer API key", true, true);
        Option secretOption = createOption("s", "secret", "API_SECRET", "paxstore developer API secret", true, true);
        Option urlOption = createOption("u", "url", "API_URL", "paxstore developer API url", true, true);
        Option appOption = createOption("a", "appname", "APP_NAME", "application name", true, true);
        Option pkgOption = createOption("p", "pkgname", "PACKAGE_NAME", "application package name", true, true);
        Option commandOption = createOption("c", "command", "COMMAND", "available commands: " + Arrays.toString(commands), true, true);
        Option helpOption = createOption("h", "help", "", "show help message", false, false);
        options.addOption(keyOption)
                .addOption(secretOption)
                .addOption(urlOption)
                .addOption(appOption)
                .addOption(pkgOption)
                .addOption(commandOption)
                .addOption(helpOption);

        // parse args
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd = commandLineParser.parse(options, args);
            // TODO: only -h should also work
            if (cmd.hasOption(helpOption)) {
                formatter.printHelp("paxstore api tool", options);
                return;
            }

            apiKey = cmd.getOptionValue(keyOption);
            apiSecret = cmd.getOptionValue(secretOption);
            apiUrl = cmd.getOptionValue(urlOption);
            appName = cmd.getOptionValue(appOption);
            pkgName = cmd.getOptionValue(pkgOption);
            command = cmd.getOptionValue(commandOption);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return;
        }

        // developer api
        developerApi = new DeveloperApi(apiUrl, apiKey, apiSecret);
        switch (command) {
            case "main":
                executeMain();
                break;
            case "getAppInfo":
                executeGetAppInfo();
                break;
            case "uploadApk":
                executeUploadApk();
                break;
            default:
                logger.error("Not a valid command. Available commands: " + Arrays.toString(commands));
        }
    }

    public static void executeMain() {
        // getAppInfo and check if app exists
        boolean appExist = false;
        AppDetailDTO appInfo = getAppInfoByName();
        if (appInfo == null) {
            return;
        } else {
            appExist = (appInfo.getPackageName() != null);
        }

        // upload apk
    }

    public static void executeGetAppInfo() {
        AppDetailDTO data = getAppInfoByName();
        if (data != null) {
            logger.info(String.format("\nid: %s\ntype: %s\nos type: %s\nstatus: %s", data.getId(), data.getType(), data.getOsType(), data.getStatus()));
        }
    }

    public static void executeUploadApk() {

    }

    public static Option createOption(String shortName, String longName, String argName, String description, boolean hasArg, boolean required) {
        return Option.builder(shortName)
                .longOpt(longName)
                .argName(argName)
                .desc(description)
                .hasArg(hasArg)
                .required(required)
                .build();
    }

    public static AppDetailDTO getAppInfoByName() {
        Result<AppDetailDTO> appInfo = developerApi.getAppInfoByName(pkgName, appName);
        if (appInfo == null) {
            logger.error("get app info by name error.");
            return null;
        } else if (appInfo.getBusinessCode() != 0) {
            logger.error("get app info failed. error code: " + appInfo.getBusinessCode());
            return null;
        } else if (appInfo.getData().getPackageName() == null) {
            logger.info(pkgName + " doesn't exist on PAXSTORE.");
        } else {
            logger.info(pkgName + " exists on PAXSTORE.");
        }
        return appInfo.getData();
    }

    public static Result<String> uploadApk(DeveloperApi developerApi, String appName, String pkgName) throws IOException {
        // prepare unzip release folder
        File zipFile = new File(releaseFolderPath);
        String destDir = zipFile.getParent(); // Extract in the same directory
        File[] extractedFiles;

        // unzip
        try {
            extractedFiles = Utils.unzip(releaseFolderPath); // Unpack
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        if (extractedFiles.length != 1) {
            logger.error("unpack the wrong release folder.");
            return null;
        }

        // get file path from release folder
        String apkSuffix = ".apk", releaseNoteSuffix = "ReleaseNote.txt", paramSuffix = ".zip";
        File releaseFolder = extractedFiles[0];
        List<String> apkFilePaths = Utils.listAndMatchFile(releaseFolder, apkSuffix);
        List<String> releaseNoteFilePaths = Utils.listAndMatchFile(releaseFolder, releaseNoteSuffix);
        List<String> paramFilePaths = Utils.listAndMatchFile(releaseFolder, paramSuffix);
        // TODO: what about automation? can be more than one apk file collected? Only one app now
        // TODO: matrix build, release and automation are two 'build' step, but share one 'release' step

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
        String releaseNote = Utils.readFileToString(releaseNoteFilePaths.get(0));
        String baseType = pkgName.contains("manager") ? APP_TYPE_NORMAL : APP_TYPE_PARAMETER;
        if (paramFilePaths.size() == 0 && baseType.equals(APP_TYPE_PARAMETER)) {
            logger.error("Parameter app but no param file found.");
            return null;
        }
        List<UploadedFileContent> paramTemplateList = paramFilePaths.stream()
                .map(FileUtils::createUploadFile)
                .collect(Collectors.toList());

        CreateApkRequest createApkRequest = new CreateApkRequest();
//        createApkRequest.setAppFile(FileUtils.createUploadFile(apkFilePath));
//        createApkRequest.setAppName(appName);
//        createApkRequest.setBaseType(baseType);
//        createApkRequest.setShortDesc(shortDesc);
//        createApkRequest.setDescription(fullDesc);
//        createApkRequest.setReleaseNotes(releaseNote);
//        createApkRequest.setChargeType(chargeType);
//
//        createApkRequest.setCategoryList(categoryList);
//        createApkRequest.setModelNameList(modelNameList);
//        createApkRequest.setScreenshotFileList(screenshotList);
//        createApkRequest.setParamTemplateFileList(paramTemplateList);
//
//
//        createApkRequest.setFeaturedImgFile(FileUtils.createUploadFile(featuredImgFilePath));
//        createApkRequest.setIconFile(FileUtils.createUploadFile(iconFilePaTH));

        return developerApi.uploadApk(createApkRequest);
    }
}