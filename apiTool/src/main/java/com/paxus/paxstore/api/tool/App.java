package com.paxus.paxstore.api.tool;

import com.pax.market.api.sdk.java.api.base.dto.ApkInfoDTO;
import com.pax.market.api.sdk.java.api.base.dto.AppDetailDTO;
import com.pax.market.api.sdk.java.api.base.dto.CodeInfoDTO;
import com.pax.market.api.sdk.java.api.base.dto.Result;
import com.pax.market.api.sdk.java.api.developer.DeveloperApi;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static final Logger logger = LoggerFactory.getLogger("ApiTool");
    // public static final String releaseFolderPath = "apiTool/src/main/release-folder";
    // public static final String releaseFolderZipPath = releaseFolderPath + ".zip";
    // release folder path is now a parameter, for easier access in github workflow
    public static final String cfgFolderPath = ".github/paxstore_api_config/";
    public static final String cfgJson = "paxstore_api_config.json";
    public static final String[] commands = new String[]{"getappinfo", "uploadapk", "createapk", "getapkbyid", "getappcategory"};

    public static String apiKey, apiSecret, apiUrl, appName, pkgName, releaseFolderPath, command;
    static DeveloperApi developerApi;

    public static void main(String[] args) {
        // print log to stdout
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");

        // options
        Options options = new Options();
        Option keyOption = createOption("k", "key", "API_KEY", "paxstore developer API key", true, true);
        Option secretOption = createOption("s", "secret", "API_SECRET", "paxstore developer API secret", true, true);
        Option urlOption = createOption("u", "url", "API_URL", "paxstore developer API url", true, true);
        Option appOption = createOption("a", "appname", "APP_NAME", "application name", true, true);
        Option pkgOption = createOption("p", "pkgname", "PACKAGE_NAME", "application package name", true, true);
        Option releaseOption = createOption("r", "releasefolder", "RELEASE_FOLDER_PATH", "release zip file path contains apk, release note and parameter templates", true, true);
        Option commandOption = createOption("c", "command", "COMMAND", "available commands: " + Arrays.toString(commands), true, true);
        Option helpOption = createOption("h", "help", "", "show help message", false, false);
        options.addOption(keyOption)
                .addOption(secretOption)
                .addOption(urlOption)
                .addOption(appOption)
                .addOption(pkgOption)
                .addOption(releaseOption)
                .addOption(commandOption)
                .addOption(helpOption);

        // parse args
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            // show helper message
            if (Utils.hasHelpOption(args, options)) {
                formatter.printHelp("java -jar /path/to/apiTool.jar", options);
                return;
            }

            CommandLine cmd = commandLineParser.parse(options, args);
            apiKey = cmd.getOptionValue(keyOption);
            apiSecret = cmd.getOptionValue(secretOption);
            apiUrl = cmd.getOptionValue(urlOption);
            appName = cmd.getOptionValue(appOption);
            pkgName = cmd.getOptionValue(pkgOption);
            releaseFolderPath = cmd.getOptionValue(releaseOption);
            command = cmd.getOptionValue(commandOption);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

        // log all args
        logger.info(String.format("accepted args:\n\tapikey: %s\n\tapisecret: %s\n\tapiurl: %s\n\tappname: %s\n\tpkgname: %s\n\trelease folder: %s\n\tcommand: %s",
                apiKey, apiSecret, apiUrl, appName, pkgName, releaseFolderPath, command));

        // developer api
        developerApi = new DeveloperApi(apiUrl, apiKey, apiSecret);
        int exeResult = 1;
        switch (command.toLowerCase()) {
            case "main":
                exeResult = executeMain();
                break;
            case "getappinfo":
                exeResult = executeGetAppInfo();
                break;
            case "uploadapk":
                exeResult = executeUploadApk();
                break;
            case "createapk":
                exeResult = executeCreateApk();
                break;
            case "getapkbyid":
                exeResult = executeGetApkById();
                break;
            case "getappcategory":
                exeResult = executeGetAppCategory();
                break;
            default:
                logger.error("Not a valid command. Available commands: " + Arrays.toString(commands));
        }
        System.exit(exeResult);
    }

    public static int executeMain() {
        // run getAppInfo, print app info
        logger.info("Check app info first");
        executeGetAppInfo();
        // create apk, but don't include param, set baseType = N
        logger.info("start createApk");
        Long id = createApk(false);
        if (id != null) {
            logger.info("created apk id: " + id);
        } else {
            return 1;
        }
        // edit apk, add params, set baseType
        logger.info("start editApk");
        String msg = editApk(id);
        if (msg != null) {
            logger.info("edit apk result: " + msg);
        } else {
            return 1;
        }
        // submit apk
        logger.info("start submitApk");
        msg = submitApk(id);
        if (msg != null) {
            logger.info("submit apk result: " + msg);
        } else {
            return 1;
        }
        return 0;
    }

    public static int executeGetAppInfo() {
        AppDetailDTO data = getAppInfoByName();
        if (data != null) {
            logger.info(String.format("\nid: %s\ntype: %s\nos type: %s\nstatus: %s", data.getId(), data.getType(), data.getOsType(), data.getStatus()));
            return 0;
        } else {
            return 1;
        }
    }

    public static int executeUploadApk() {
        String data = uploadApk();
        if (data != null) {
            logger.info("message: " + data);
            return 0;
        } else {
            return 1;
        }
    }

    public static int executeCreateApk() {
        Long id = createApk(true);
        if (id != null) {
            logger.info("created apk id: " + id);
            return 0;
        } else {
            return 1;
        }
    }

    public static int executeGetApkById() {
        long id;
        try {
            id = Long.parseLong(Utils.input("Please input apk id\n"));
        } catch (NumberFormatException e) {
            logger.error("not a valid id.");
            return 1;
        }
        ApkInfoDTO data = getApkById(id);
        if (data != null) {
            logger.info("message: " + data);
            return 0;
        } else {
            return 1;
        }
    }

    public static int executeGetAppCategory() {
        List<String> data = getAppCategory();
        if (data != null) {
            logger.info("All category code in alphabetical order:\n" + String.join("\n", data));
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * call getAppInfoByName and check
     * @return appInfo if success, else null
     */
    public static AppDetailDTO getAppInfoByName() {
        Result<AppDetailDTO> appInfo = developerApi.getAppInfoByName(pkgName, appName);
        if (appInfo == null) {
            logger.error("call getAppInfoByName API error.");
            return null;
        } else if (appInfo.getBusinessCode() != 0) {
            logger.error("get app info failed. error code: " + appInfo.getBusinessCode() + ", error message: " + appInfo.getMessage());
            return null;
        } else if (appInfo.getData() == null || appInfo.getData().getPackageName() == null) {
            logger.info(appName + "(" + pkgName + ") " + "doesn't exist on PAXSTORE. Check the app name and the package name, or an new app will be created.");
        } else {
            logger.info(appName + "(" + pkgName + ") " + "exists on PAXSTORE.");
        }
        return appInfo.getData();
    }

    /**
     * call uploadApk and check
     * this will create app if doesn't exist, upload the apk, and submit for approval
     * app is in 'pending status'
     * @return upload message if success, else null
     */
    public static String uploadApk() {
        Result<String> result;
        try {
            result = developerApi.uploadApk(Utils.createApkRequest(appName));  // set app name is mandatory, pass the command line param
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        if (result == null) {
            logger.error("call uploadApk API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("upload apk failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        }
        return "upload apk success.";  // getData(), getMessage() both returns null
    }

    /**
     * call getAppinfo to get id, and createApk, and check
     * this will only upload the apk, not submitted for approval
     * app is in 'draft' status
     * @param addParam true if include param files, else false
     * @return apk id of the created apk
     */
    public static Long createApk(boolean addParam) {
        AppDetailDTO appInfo = getAppInfoByName();
        if (appInfo == null || appInfo.getId() == null) {
            logger.error("get app id failed, cannot create apk");
            return null;
        }
        long id = appInfo.getId();
        Result<Long> result;
        try {
            result = developerApi.createApk(Utils.createSingleApkRequest(id, addParam));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        if (result == null) {
            logger.error("call create API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("create apk failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        } else {
            logger.info("create apk success.");
        }
        return result.getData();
    }

    /**
     * call deleteApk to delete a draft apk
     * id is input by user during runtime
     * should be used after createApk which returns an apk id
     * @param id apk id
     * @return response if delete success, else null
     */
    public static String deleteApk(long id) {
        Result<String> result = developerApi.deleteApk(id);
        if (result == null) {
            logger.error("call deleteApk API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("delete apk failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        }
        return "delete apk success.";  // getData(), getMessage() both returns null
    }

    public static ApkInfoDTO getApkById(long id) {
        Result<ApkInfoDTO> result = developerApi.getApkById(id);
        if (result == null) {
            logger.error("call getApkById API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("get apk by id failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        } else {
            logger.info("get apk by id success.");
            return result.getData();
        }
    }

    public static List<String> getAppCategory() {
        Result<CodeInfoDTO> result = developerApi.getAppCategory();
        if (result == null) {
            logger.error("call getApkById API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("get apk by id failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        } else {
            logger.info("get app category success.");
            // return a list of sorted codes
            List<String> codeList = result.getPageInfo().getDataSet().stream().map(CodeInfoDTO::toString).collect(Collectors.toList());
            Collections.sort(codeList);
            return codeList;
        }
    }

    public static String editApk(long id) {
        Result<String> result;
        try {
            result = developerApi.editApk(Utils.editSingleApkRequest(id));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        if (result == null) {
            logger.error("call edit API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("edit apk failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        }
        return "edit apk success.";  // getData(), getMessage() both returns null
    }

    public static String submitApk(long id) {
        Result<String> result;
        try {
            result = developerApi.submitApk(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        if (result == null) {
            logger.error("call submitapk API error.");
            return null;
        } else if (result.getBusinessCode() != 0) {
            logger.error("submit apk failed. error code: " + result.getBusinessCode() + ", error message: " + result.getMessage());
            return null;
        }
        return "submit apk success.";  // getData(), getMessage() both returns null
    }

    /**
     * helper to create option
     */
    public static Option createOption(String shortName, String longName, String argName, String description, boolean hasArg, boolean required) {
        return Option.builder(shortName)
                .longOpt(longName)
                .argName(argName)
                .desc(description)
                .hasArg(hasArg)
                .required(required)
                .build();
    }
}