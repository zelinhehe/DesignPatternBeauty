package com.example.C14_ApiAuthenticator;

import com.example.C14_ApiAuthenticator.util.UrlUtil;

import java.util.Map;

public class ApiRequest {
    private String baseUrl;
    private String token;
    private String appId;
    private long timestamp;

    public ApiRequest(String baseUrl, String appId, String token, long timestamp) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.appId = appId;
        this.timestamp = timestamp;
    }

    public static ApiRequest buildFromUrl(String url) {
        UrlUtil.UrlEntity urlEntity = UrlUtil.parse(url);
        Map<String, String> params = urlEntity.getParams();
        return new ApiRequest(urlEntity.getBaseUrl(), params.get("token"),
                params.get("appId"), Long.parseLong(params.get( "timestamp")));
    }

    public String getBaseUrl() {
        return baseUrl;
    }
    public String getToken() {
        return token;
    }
    public String getAppId() {
        return appId;
    }
    public long getTimestamp() {
        return timestamp;
    }
}
