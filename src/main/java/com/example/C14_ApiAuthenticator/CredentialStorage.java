package com.example.C14_ApiAuthenticator;

import java.util.HashMap;
import java.util.Map;

interface CredentialStorage {
    String getPasswordByAppId(String appId);
}

class MysqlCredentialStorage implements CredentialStorage{

    private static Map<String, String> passwordMap = new HashMap<>();

    static {
        passwordMap.put("1001", "PASS-1001");
        passwordMap.put("1002", "PASS-1002");
    }

    @Override
    public String getPasswordByAppId(String appId) {
        return passwordMap.get(appId);
    }
}
