package com.paxus.paxstore.api.tool;

import com.pax.market.api.sdk.java.api.base.dto.AppDetailDTO;
import com.pax.market.api.sdk.java.api.base.dto.Result;
import com.pax.market.api.sdk.java.api.developer.DeveloperApi;
import com.pax.market.api.sdk.java.api.developer.dto.CreateApkRequest;
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


public class App {
    public static final Logger logger = LoggerFactory.getLogger(App.class);
    public static final String releaseFolder = "release-folder";

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
        Option keyOption = createOption("k", "key", "API_KEY", "Paxstore developer API key", true);
        Option secretOption = createOption("s", "secret", "API_SECRET", "Paxstore developer API secret", true);
        Option urlOption = createOption("u", "url", "API_URL", "Paxstore developer API url", true);
        Option appOption = createOption("a", "appname", "APP_NAME", "application name", true);
        Option pkgOption = createOption("p", "pkgname", "PACKAGE_NAME", "application package name", true);
        options.addOption(keyOption)
                .addOption(secretOption)
                .addOption(urlOption)
                .addOption(appOption)
                .addOption(pkgOption);

//        // print help info
//        HelpFormatter helpFormatter = new HelpFormatter();
//        options.addOption("?", "help", false, "Display help information");
//        helpFormatter.printHelp("apiTool -k apiKey -s apiSecret -u apiUrl -a appName -p packageName", options);

        // parse args
        String apiKey, apiSecret, apiUrl, appName, pkgName;
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine cmd = commandLineParser.parse(options, args);
            apiKey = cmd.getOptionValue(keyOption);
            apiSecret = cmd.getOptionValue(secretOption);
            apiUrl = cmd.getOptionValue(urlOption);
            appName = cmd.getOptionValue(appOption);
            pkgName = cmd.getOptionValue(pkgOption);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return;
        }

        // developer api
        DeveloperApi developerApi = new DeveloperApi(apiUrl, apiKey, apiSecret);

        // getAppInfo and check if app exists
        boolean exist = false;
        Result<AppDetailDTO> appInfo = getAppInfoByName(developerApi, pkgName, appName);
        if (appInfo.getBusinessCode() != 0) {
            logger.error("get app info failed. error code: " + appInfo.getBusinessCode());
            return;
        } else if (appInfo.getData().getPackageName() == null) {
            logger.info(pkgName + " doesn't exist on PAXSTORE. Create new application.");
        } else {
            logger.info(pkgName + " exists on PAXSTORE. Upload new version.");
            exist = true;
        }
    }

    public static Option createOption(String shortName, String longName, String argName, String description, boolean required) {
        return Option.builder(shortName)
                .longOpt(longName)
                .argName(argName)
                .desc(description)
                .hasArg()
                .required(required)
                .build();
    }

    public static Result<AppDetailDTO> getAppInfoByName(DeveloperApi developerApi, String packageName, String appName) {
        return developerApi.getAppInfoByName(packageName, appName);
    }
}