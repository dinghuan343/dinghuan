
package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.user.core.service.GoogleLoginService;
import com.oppo.cdo.instant.platform.user.domain.entity.GoogleUserInfo;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GoogleUtils {

//    public static String url = "https://oauth2.googleapis.com/token";
//    public static String clientId = "439052946921-7ima43q48jlbjllu1fhdoks7dut1gldc.apps.googleusercontent.com";
//    public static String clientSecret = "xWHxUfaRlooO2y6LoXnh506D";
    public static String redirectUri = "";

    private static final Logger logger = LoggerFactory.getLogger(GoogleUtils.class);


    public  static  Boolean   checkIdToken(String idCode,String clientId) throws  Exception{
        // 检查idToken
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance()).setAudience(Collections.singleton(clientId)).build();
        GoogleIdToken  idTokenResponse = verifier.verify(idCode);
        if (idTokenResponse == null) {
            logger.warn("google登陆授权验证失败，idTokenResponse为空请稍后再试");
            return false;
        }
        GoogleIdToken.Payload payload = idTokenResponse.getPayload();
        String audId = (String)payload.get("aud");
        if (!audId.equals(clientId)) {
            logger.warn("google登陆授权验证失败，audId not equals APPID,audId:{}",audId);
            return false;
        }
        return true;
    }


    // 刷新token

    public static  GoogleUserInfo  refreshAccessToken (String refreshToken,String url,String clientId,String clientSecret) throws Exception{

        // 刷新token
        GoogleTokenResponse refreshTokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                refreshToken,
                clientId,
                clientSecret
        ).execute();
        GoogleUserInfo googleUserInfo = new GoogleUserInfo();
        String accessToken = refreshTokenResponse.getAccessToken();
        String refreshTokenGagin = refreshTokenResponse.getRefreshToken();
        Long expiresInSeconds = refreshTokenResponse.getExpiresInSeconds();
        googleUserInfo.setAccessToken(refreshTokenResponse.getAccessToken());
        googleUserInfo.setRefreshToken(refreshTokenGagin);
        googleUserInfo.setExpiresInSeconds(expiresInSeconds);
        return  googleUserInfo;
    }

    public static GoogleUserInfo getInfoByCode(String authCode,String url,String clientId,String clientSecret) {
        try {
            GoogleTokenResponse tokenResponse =
                    new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                            url,
                            clientId,
                            clientSecret,
                            authCode,
                            redirectUri).execute();
            GoogleUserInfo googleUserInfo = new GoogleUserInfo();
            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();
            Long expiresInSeconds = tokenResponse.getExpiresInSeconds();
            googleUserInfo.setAccessToken(tokenResponse.getAccessToken());
            googleUserInfo.setRefreshToken(refreshToken);
            googleUserInfo.setExpiresInSeconds(expiresInSeconds);

            // Get profile info from ID token
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();  // Use this value as a key to identify a user.
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            String age = (String) payload.get("age");
            String birthday = (String) payload.get("birthday");
            String sex = (String) payload.get("gender");
            googleUserInfo.setUserId(userId);
            googleUserInfo.setEmail(email);
            googleUserInfo.setName(name);
            googleUserInfo.setPictureUrl(pictureUrl);
            googleUserInfo.setLocale(locale);
            googleUserInfo.setFamilyName(familyName);
            googleUserInfo.setGivenName(givenName);
            if (!StringUtils.isEmpty(age)) {
                googleUserInfo.setAge(Integer.parseInt(age));
            }
            googleUserInfo.setSex(sex);
            googleUserInfo.setBirthday(birthday);
            return googleUserInfo;
        } catch (Exception e) {
            logger.warn("googleUtils authcode failed", e);
            return null;
        }
    }


}

