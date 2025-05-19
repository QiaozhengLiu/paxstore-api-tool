# paxstore-api-tool
A jar tool calls paxstore developer API to upload apps to paxstore.

## Reference
This tool calls paxstore develop sdk: https://github.com/paxstore/paxstore-develop-sdk/blob/master/README.md

## Usage
- clean: `.\gradlew clean`
- build: `.\gradlew assemble`, result jar is at `.\bin\apiTool-x.x.x.jar` or `.\apiTool\build\libs\apiTool-x.x.x-all.jar`
- run: `java -Xmx1536m -jar \path\to\apiTool-x.x.x.jar --url API_URL --key API_KEY --secret API_SECRET --appname APP_NAME --pkgname PACKAGE_NAME --command COMMAND --releasefolder RELEASE_FOLDER`
- help message: `java -jar \path\to\apiTool-x.x.x.jar -h`

## Command Line Parameters
### Paxstore Credentials
- url: paxstore developer api url
- key: paxstore developer api key
- secret: paxstore developer api secret

Credentials are available at paxstore developer center -> account center -> developer SDK integration.

Only world marketplace credential is allowed to upload apps to paxstore world marketplace.

DEV developers can test the tool with their credentials on paxstore SIT marketplace.

### Other Parameters
- app name: app name on paxstore. This is not apk name. Apk name can be changed in each version.
- package name: pacakge name on paxstore. This is application id in code.
- command: call different APIs. See next section for available commands.
- release folder: release folder name with absolute path. Don't include the suffix '.zip'. This is the release zip file name contains apk, parameter template, release note. The zip file should follow BroadPOS release folder structure. Use Absolute path. Release folder is only used when using `createapk` or `uploadapk` and can be set to any value if other commands are used.

### Available Commands
- `getappinfo`: get app info. Use this to check if the app name and package name exists on paxstore.
- `getappcategory`: get all business categories on paxstore.
- `getapkbyid`: get apk info by input apk id. This is not same as app id shown on paxstore. Currently apk id can only be seen after `createapk` is successfully called as a returned value.
- `createapk`: upload an app version without submit. Upload will be failed if the app name with package name is not in paxstore. New version is in `Draft` and allowed to edit or delete.
- `uploadapk`: upload an app version and submit for approval, and create a new one if the app name with package name is not in paxstore. New version is in `Pending` and not allowed to edit or delete.

## Config Folder
- Run `createapk` and `uploadapk` need settings in config folder.
- A configuration JSON file is required at `/path/to/paxstore_api_config/paxstore_api_config.json`. This file contains app details not included in the release folder but required for upload, including app description, business category, supported models, sample screenshot file paths, and app icon file path. Each product flavor of the app should have a corresponding configuration folder under `/path/to/paxstore_api_config/FOLDER_NAME`. Each folder name should match `variantName` field in the above json file. Each folder contains screenshots, feature, app icon files. File names should also match `screenshotFilePaths`, `featureImgFilePath`, `iconFilePath` fields in the above json file.
- See this repository's `.github/paxstore_api_config` for instance.