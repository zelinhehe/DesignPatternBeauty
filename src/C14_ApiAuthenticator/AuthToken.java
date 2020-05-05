package C14_ApiAuthenticator;

import C14_ApiAuthenticator.util.Md5Util;

public class AuthToken {
    private static final long DEFAULT_EXPIRED_TIME_INTERVAL = 60 * 1000;

    private String token;
    private long createTime;
    private long expiredTimeInterval = DEFAULT_EXPIRED_TIME_INTERVAL;

    public AuthToken(String token, long createTime) {
        this.token = token;
        this.createTime = createTime;
    }

    public AuthToken(String token, long createTime, long expiredTimeInterval) {
        this(token, createTime);
        this.expiredTimeInterval = expiredTimeInterval;
    }

    public static AuthToken generate(String originalUrl, String appId, String password, long timestamp) {
        String token = Md5Util.md5(originalUrl + appId + password + timestamp);
        return new AuthToken(token, timestamp);
    }

    public String getToken() {
        return token;
    }

    public boolean isExpired() {
        return createTime + expiredTimeInterval < System.currentTimeMillis();
    }

    public boolean match(AuthToken authToken) {
        return token.equals(authToken.getToken());
    }
}


