package com.impinj.datahub.itemsense;

/**
 * Created by jcombopi on 1/11/16.
 */
public class ItemSenseConfiguration {
    private String baseUrl;
    private String userName;
    private String password;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
