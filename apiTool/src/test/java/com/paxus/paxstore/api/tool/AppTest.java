package com.paxus.paxstore.api.tool;

import static com.pax.market.api.sdk.java.api.constant.Constants.APP_TYPE_PARAMETER;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.pax.market.api.sdk.java.api.base.dto.ApkInfoDTO;
import com.pax.market.api.sdk.java.api.base.dto.AppDetailDTO;
import com.pax.market.api.sdk.java.api.base.dto.Result;
import com.pax.market.api.sdk.java.api.developer.dto.CreateApkRequest;
import com.pax.market.api.sdk.java.api.io.UploadedFileContent;
import com.pax.market.api.sdk.java.api.util.FileUtils;


import com.pax.market.api.sdk.java.api.developer.DeveloperApi;

public class AppTest {
    String url = "https://api.paxstore.us/p-market-api";
    String apiKey = "6SUXXO5L665X6XH5EP2V";
    String apiSecret = "6SZOU9FJFZNK8QCOWJP3EIQGPQRI1CL4HCW3FML8";
    String appName = "Test PAXSTORE Dev API";
    DeveloperApi developerApi = new DeveloperApi(url, apiKey, apiSecret);

    @Test
    public void test_paxstore_developer_api_uploadApk() {
        // create a new app and upload the first apk, version

        String appFilePath = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\test_paxstore_developer_api_versioncode2.apk";
        String baseType = APP_TYPE_PARAMETER;
        String shortDesc = "Test PAXSTORE Dev API: short description";
        String fullDesc = "Test PAXSTORE Dev API: full description.";
        String releaseNote = "Test PAXSTORE Dev API: This is release note \n item1 \n item2 \n item3";
        int chargeType = 0;

        List<String> categoryList = Arrays.asList("WL_PS", "WL_SK");
        List<String> modelNameList = Arrays.asList("A920", "A920Max", "A80", "L1400");
        String screenshotFilePath1 = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\semi-idle-0.png";
        String screenshotFilePath2 = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\semi-idle-1.png";
        String screenshotFilePath3 = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\semi-idle-2.png";
        List<UploadedFileContent> screenshotList = Arrays.asList(
                FileUtils.createUploadFile(screenshotFilePath1),
                FileUtils.createUploadFile(screenshotFilePath2),
                FileUtils.createUploadFile(screenshotFilePath3)
        );

        String paramTemplateFilePath1 = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\restaurant.zip";
        String paramTemplateFilePath2 = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\retail.zip";
        String paramTemplateFilePath3 = "C:\\Users\\qiaozheng.liu\\Desktop\\apks\\lodging.zip";
        List<UploadedFileContent> paramTemplateList = Arrays.asList(
                FileUtils.createUploadFile(paramTemplateFilePath1),
                FileUtils.createUploadFile(paramTemplateFilePath2),
                FileUtils.createUploadFile(paramTemplateFilePath3)
        );

        CreateApkRequest createApkRequest = new CreateApkRequest();
        createApkRequest.setAppFile(FileUtils.createUploadFile(appFilePath));
        createApkRequest.setAppName(appName);
        createApkRequest.setBaseType(baseType);
        createApkRequest.setShortDesc(shortDesc);
        createApkRequest.setDescription(fullDesc);
        createApkRequest.setReleaseNotes(releaseNote);
        createApkRequest.setChargeType(chargeType);

        createApkRequest.setCategoryList(categoryList);
        createApkRequest.setModelNameList(modelNameList);
        createApkRequest.setScreenshotFileList(screenshotList);
        createApkRequest.setParamTemplateFileList(paramTemplateList);


        createApkRequest.setFeaturedImgFile(FileUtils.createUploadFile(screenshotFilePath1));
        createApkRequest.setIconFile(FileUtils.createUploadFile(screenshotFilePath1));

        Result<String> result = developerApi.uploadApk(createApkRequest);
        String appId = result.getData();
        System.out.println("appid: " + result);
//        assertEquals(0, result.getBusinessCode());
    }

    @Test
    public void test_paxstore_developer_api_getApkById() {
        long apkId = 1662255915597862L;
        Result<ApkInfoDTO> apkInfo = developerApi.getApkById(apkId);
        ApkInfoDTO data = apkInfo.getData();
        System.out.println("getApkById data: " + data);
    }

    @Test
    public void test_paxstore_developer_api_getAppInfoByName() {
        String packageName = "com.test_paxstore_developer_api";
        Result<AppDetailDTO> appDetail = developerApi.getAppInfoByName(packageName, appName);
        AppDetailDTO data = appDetail.getData();
        System.out.println("getAppInfoByName data: " + data);
    }

    @Test
    public void test_paxstore_developer_api_deleteApk() {
        Result<String> result = developerApi.deleteApk(2L);
        System.out.println("deleteApk result: " + result);
    }

}
