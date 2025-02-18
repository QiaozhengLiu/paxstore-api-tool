package com.paxus.paxstore.api.tool;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pax.market.api.sdk.java.api.developer.DeveloperApi;

public class App {
    static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws ParseException {
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

        Options options = new Options();
        options.addOption(new Option("k", "key", true, "Paxstore developer API key."));
        options.addOption(new Option("s", "secret", true, "Paxstore developer API secret."));
        options.addOption(new Option("u", "url", true, "Paxstore developer API url."));
        options.addOption(new Option("a", "appname", true, "Application name."));
        options.addOption(new Option("p", "pkgname", true, "Application package name."));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String key = cmd.getParsedOptionValue("k");
        String secret = cmd.getParsedOptionValue("s");
        String url = cmd.getParsedOptionValue("u");
        String appName = cmd.getParsedOptionValue("a");
        String pkgName = cmd.getParsedOptionValue("p");

        if (key != null) {
            logger.info("api key: " + key);
        } else {
            logger.error("no valid api key");
        }
    }
}