package com.paxus.paxstore.api.tool;

import static com.paxus.paxstore.api.tool.App.logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Read info in paxstore-api-cfg.json used in uploadApk
 */
public class Config {
    public String variantName;
    public int chargeType;
    public String baseType;
    public String shortDesc, fullDesc;
    public List<String> categoryList, modelNameList;
    public String featureImgFilePath, iconFilePath;
    public List<String> screenshotFilePaths;

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
