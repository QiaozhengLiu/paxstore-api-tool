# paxstore-api-tool
A jar tool calls PAXSTORE developer API.

## Reference
paxstore develop sdk: https://github.com/PAXSTORE/paxstore-develop-sdk/blob/master/README.md

## Usage
- clean: `.\gradlew clean`
- build: `.\gradlew assemble`
- run: `java -jar .\apiTool\build\libs\apiTool-1.0.0-all.jar --url API_URL --key API_KEY --secret API_SECRET --appname APP_NAME --pkgname PACKAGE_NAME --command COMMAND`

## Command Line Parameters
- API_URL: paxstore developer api url
- API_KEY: paxstore developer api key
- API_SECRET: paxstore developer api secret

All three above available at: paxstore developer center -> account center -> developer SDK integration

- APP_NAME: app name on paxstore
- PACKAGE_NAME: package name of the app
- command: call different APIs
