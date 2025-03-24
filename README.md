# paxstore-api-tool
A jar tool calls PAXSTORE developer API. This is used in BroadPOS app release workflow to upload release apks to PAXSTORE.

## Reference
paxstore develop sdk: https://github.com/PAXSTORE/paxstore-develop-sdk/blob/master/README.md

## Usage
- clean: `.\gradlew clean`
- build: `.\gradlew assemble`
- copy tool `.\apiTool\build\libs\apiTool-1.0.0-all.jar` and rename for your further usage, or:
- run: `java -jar .\apiTool\build\libs\apiTool-1.0.0-all.jar --url API_URL --key API_KEY --secret API_SECRET --appname APP_NAME --pkgname PACKAGE_NAME --command COMMAND --releasefolder RELEASE_FOLDER`

## Command Line Parameters
- url: paxstore developer api url
- key: paxstore developer api key
- secret: paxstore developer api secret

All three above available at: paxstore developer center -> account center -> developer SDK integration

- app name: app name on paxstore. This is not apk name. Apk name can be revised in each version.
- package name: pacakge name on paxstore. This is application id in code.
- command: call different APIs. 
  - `getappinfo`: get app info. This can check if the app name with package is correct.
  - `createapk`: upload without submit. New version is in `Draft` allowed to edit or delete.
  - `uploadapk`: to upload and submit for approval, and create a new one if the app name with package name is not in paxstore. New version is in `Pending` and not allowed to edit or delete.
- release folder: the release zip file name contains apk, parameter template, release note. This is compliant with BroadPOS release folder. Use Absolute path. Release folder is only used when using `createapk` or `uploadapk`.

## Other Configurations
- A configuration JSON file is required at `/path/to/paxstore_api_config/paxstore_api_config.json`. The file name is hardcoded in the tool. This file contains application details not included in the release folder but required for upload, such as app description, business category, supported models, sample screenshot file paths, and app icon file path. Each product flavor of the app should have a corresponding configuration folder under `/path/to/paxstore_api_config/FOLDER_NAME`. Each folder name should match `variantName` field in the above json file. Each folder contains screenshots, feature, app icon files. File names should also match `screenshotFilePaths`, `featureImgFilePath`, iconFilePath` fields in the above json file.
- See this repository's `.github/paxstore_api_config` for instance.