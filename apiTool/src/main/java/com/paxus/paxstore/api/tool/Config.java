package com.paxus.paxstore.api.tool;

import static com.paxus.paxstore.api.tool.App.logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Read info in paxstore-api-cfg.json used in uploading requests
 */
public class Config {
    public String variantName;
    public String apkName;
    public int chargeType;
    public String baseType;
    public String shortDesc, fullDesc;
    public List<String> categoryList, modelNameList;
    public String featureImgFilePath, iconFilePath;
    public List<String> screenshotFilePaths;
    // control the parameter template order when pushing tasks on Paxstore, especially the first one, will be the default template
    // a list of zip file names to upload: [retail.zip, restaurant, zip, lodging.zip]
    // the first element is the default template
    public List<String> paramOrder;

    // Default constructor required for Jackson
    public Config() {}

    public static Config loadJson(String filePath, String pkgName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Config> data = objectMapper.readValue(new File(filePath), new TypeReference<Map<String, Config>>() {});
            return data.getOrDefault(pkgName, null);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
