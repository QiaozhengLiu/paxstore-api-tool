package com.paxus.paxstore.api.tool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.pax.market.api.sdk.java.api.developer.DeveloperApi;

/**
 * Unit test commands.
 * Use FDRC SSF Test in SIT MP to test APIs. Apk version is one higher than the version on Paxstore.
 * If you're running unit tests, you can prepare other release folders with the apk matching an app on Paxstore and test.
 */
public class AppTest {
    String url = "https://api.paxstore.us/p-market-api";
    String apiKey = "";
    String apiSecret = "";
    String appName = "";
    String pkgName = "";
    String releaseFolder = "";
    DeveloperApi developerApi = new DeveloperApi(url, apiKey, apiSecret);

    @Test
    public void test_command_create_edit_delete() {
        Long id = App.createApk(false);
        assertNotEquals(null, id);
        String msg = App.editApk(id);
        assertEquals("edit apk success.", msg);
        msg = App.deleteApk(id);
        assertEquals("delete apk success.", msg);
    }

}
