package com.oppo.cdo.instant.platform.user.core.config;

import java.util.Properties;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesDynamicConfig;
import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesPropertiesConfig;
import com.oppo.cdo.instant.platform.common.base.conf.BaseHeraclesConfig;
import com.oppo.cdo.instant.platform.common.base.util.SpringBeanUtil;

@Component
@DependsOn("springBeanUtil")
public class HeraclesConfig extends BaseHeraclesConfig {

    public static final String APP_FILE_NAME = "application.properties";

    @HeraclesPropertiesConfig(filename = "mq.properties")
    private Properties         mqProperties;
    private static HeraclesConfig heraclesConfig;
    /**
     * facebook拨测uid
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "test.facebook.uid", defaultValue = "445311488")
    private String             faceBookTestUid;
    /**
     * google拨测uid
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "test.facebook.uid", defaultValue = "445311489")
    private String             googleBookTestUid;

    /**
     * faceBook拨测token
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "test.facebook.token", defaultValue = "XX.FB_bE9MM3V3WHdka3VLNGpYaE1aODZLSFpzVEIyTmUvUXJRNGdTREUwbDNubWNibGVhZDhtTUxVZkdnM2pvbUF3SmtoMEdnNXREcTJpbDFtOVRKdTZRbGc9PQ")
    private String             testFaceBookToken;
    /**
     * google拨测token
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "test.google.token", defaultValue = "XX.GL_TkhiaHhFVk8zZ3YwNTQ1RW5CME5vMTJZSU9ML2hUd3pURWRqVmtiZElDR1dkK0R6Q3kvenl3czVEVWlmalRDag")
    private String             testGoogleToken;
    /**
     * token对称加密秘钥
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "token.secret.key", defaultValue = "")
    private String             tokenSecretKey;

    /**
     * 是否允许测试用户
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "allow.test.user", defaultValue = "false")
    private Boolean            isAllowTestUser;

    /**
     * facebook appid
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "facebook.appId", defaultValue = "")
    private String             facebookAppId;

    /**
     * facebook secret
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "facebook.secret", defaultValue = "")
    private String             facebookSecret;

    /**
     * google appid
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "google.appId", defaultValue = "")
    private String             googleAppId;

    /**
     * facebook secret
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "google.secret", defaultValue = "")
    private String             googleSecret;

    /**
     * google oauth url
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "google.oauth.url", defaultValue = "https://oauth2.googleapis.com/token")
    private String             googleOauthUrl;

    /**
     * facebook oauth url
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "facebook.oauth.url", defaultValue = "https://graph.facebook.com")
    private String             facebookPRE_URL;

    /**
     * token 失效时间
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.token.expire.time", defaultValue = "7200")
    private Integer            userTokenExpiresTime;

    /**
     * google apikey
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "google.apikey.value", defaultValue = "")
    private String             googleApiKey;

    /**
     * 测试环境批量固定的测试tokens
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.test.tokens", defaultValue = "")
    private String             testTrueTokens;

    /**
     * 敏感词productId
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.syn.productId")
    private Integer            sensitiveSynProductId;

    /**
     * 敏感词 key
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.syn.apikey", defaultValue = "")
    private String             sensitiveSynApikey;

    /**
     * 敏感词 secret
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.syn.apisecret", defaultValue = "")
    private String             sensitiveSynApisecret;

    /**
     * 敏感词url
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.syn.url", defaultValue = "")
    private String             sensitiveSynUrl;

    /**
     * 昵称是否检测敏感词
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.nickName.sensitive", defaultValue = "false")
    private Boolean            userNickeNameCheck;

    /**
     * 个性签名是否检测敏感词
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.userSign.sensitive", defaultValue = "false")
    private Boolean            userSignCheck;

    /**
     * 个性签名是否检测头像
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.userAvatar.sensitive", defaultValue = "false")
    private Boolean            userAvatarCheck;

    /**
     * 图片检测productId
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.image.productId")
    private Integer            sensitiveImageProductId;

    /**
     * 图片检测 key
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.image.apikey", defaultValue = "")
    private String             sensitiveImageApikey;

    /**
     * 图片检测 secret
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.image.apisecret", defaultValue = "")
    private String             sensitiveImageApisecret;

    /**
     * 图片检测url
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "sensitive.image.url", defaultValue = "")
    private String             sensitiveImageUrl;

    /**
     * 第三方默认头像
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "thirdPart.defualt.pricture", defaultValue = "")
    private String             thirdPartDefaultPicture;

    /**
     * 系统默认头像
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.default.picture", defaultValue = "")
    private String             userDefaultPicture;

    /**
     * 昵称敏感词替换
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.sensitive.word", defaultValue = "Display name blocked")
    private String             sensitiveWordText;

    /**
     * 签名敏感词替换
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "usersign.sensitive.word", defaultValue = "Personal mantra blocked")
    private String             userSignWordText;

    /**
     * 用户在线缓存时间
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "user.online.cache.time", defaultValue = "21600")
    private Integer            userOnlineCacheTime;

    /**
     * 用户oid过期时间
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "aid.expires.seconds.time", defaultValue = "614800")
    private Integer            aidExpiresSecondsTime;

    /**
     * facebook accesstoken 过期时间(s)
     */
    @HeraclesDynamicConfig(fileName = APP_FILE_NAME, key = "facebooktoken.expires.time", defaultValue = "4320000")
    private Integer            accessTokenExpire;

    public Boolean getAllowTestUser() {
        return isAllowTestUser;
    }

    public Properties getMqProperties() {
        return mqProperties;
    }

    public Integer getUserOnlineCacheTime() {
        return userOnlineCacheTime;
    }

    public static String getAppFileName() {
        return APP_FILE_NAME;
    }

    public String getFaceBookTestUid() {
        return faceBookTestUid;
    }

    public String getGoogleBookTestUid() {
        return googleBookTestUid;
    }

    public String getTestFaceBookToken() {
        return testFaceBookToken;
    }

    public String getTestGoogleToken() {
        return testGoogleToken;
    }

    public String getTokenSecretKey() {
        return tokenSecretKey;
    }

    public Boolean getIsAllowTestUser() {
        return isAllowTestUser;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public String getFacebookSecret() {
        return facebookSecret;
    }

    public String getGoogleAppId() {
        return googleAppId;
    }

    public String getGoogleSecret() {
        return googleSecret;
    }

    public String getGoogleOauthUrl() {
        return googleOauthUrl;
    }

    public String getFacebookPRE_URL() {
        return facebookPRE_URL;
    }

    public Integer getUserTokenExpiresTime() {
        return userTokenExpiresTime;
    }

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    public String getTestTrueTokens() {
        return testTrueTokens;
    }

    public Integer getSensitiveSynProductId() {
        return sensitiveSynProductId;
    }

    public String getSensitiveSynApikey() {
        return sensitiveSynApikey;
    }

    public String getSensitiveSynApisecret() {
        return sensitiveSynApisecret;
    }

    public String getSensitiveSynUrl() {
        return sensitiveSynUrl;
    }

    public Boolean getUserNickeNameCheck() {
        return userNickeNameCheck;
    }

    public Boolean getUserSignCheck() {
        return userSignCheck;
    }

    public String getThirdPartDefaultPicture() {
        return thirdPartDefaultPicture;
    }

    public String getUserDefaultPicture() {
        return userDefaultPicture;
    }

    public Integer getAidExpiresSecondsTime() {
        return aidExpiresSecondsTime;
    }

    public Integer getSensitiveImageProductId() {
        return sensitiveImageProductId;
    }

    public String getSensitiveImageApikey() {
        return sensitiveImageApikey;
    }

    public String getSensitiveImageApisecret() {
        return sensitiveImageApisecret;
    }

    public String getSensitiveImageUrl() {
        return sensitiveImageUrl;
    }

    public Boolean getUserAvatarCheck() {
        return userAvatarCheck;
    }

    public String getSensitiveWordText() {
        return sensitiveWordText;
    }

    public String getUserSignWordText() {
        return userSignWordText;
    }

    public Integer getAccessTokenExpire() {
        return accessTokenExpire;
    }
    public static HeraclesConfig getHeraclesConfig() {
        if (heraclesConfig == null) {
            heraclesConfig = SpringBeanUtil.getBeanByType(HeraclesConfig.class);
        }
        return heraclesConfig;
    }
}
