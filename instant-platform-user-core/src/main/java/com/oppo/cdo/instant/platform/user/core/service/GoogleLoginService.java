package com.oppo.cdo.instant.platform.user.core.service;

import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.getOverSeaAge;
import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.getOverSeaSex;
import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.parseBirthdayJSON;
import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.parseEmailJSON;
import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.parseGenderJSON;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.core.util.GoogleUtils;
import com.oppo.cdo.instant.platform.user.domain.dto.req.LoginReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.LoginRspDto;
import com.oppo.cdo.instant.platform.user.domain.entity.GoogleUserInfo;
import com.oppo.cdo.instant.platform.user.domain.type.LoginType;
import com.oppo.framework.okhttp.HttpClient;

@Component
public class GoogleLoginService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleLoginService.class);


    private static String OAUTH_URL;

    private static  String APPID;

    private static String APPSECRET;

    private static  String APIKEY;

    @Autowired
    private HeraclesConfig heraclesConfig;

    @Autowired
    private HttpClient httpClient;
    @PostConstruct
    public void intGoogleService() throws Exception{
        APPID = heraclesConfig.getGoogleAppId();
        APPSECRET = heraclesConfig.getGoogleSecret();
        OAUTH_URL = heraclesConfig.getGoogleOauthUrl();
        APIKEY = heraclesConfig.getGoogleApiKey();
        if (StringUtils.isEmpty(APPID)) {
            logger.warn("get google APPID failed!");
            throw new Exception("get google APPID failed!");
        }
        if (StringUtils.isEmpty(APPSECRET)) {
            logger.warn("get google APPSECRET failed!");
            throw new Exception("get google APPSECRET failed!");
        }
        if (StringUtils.isEmpty(OAUTH_URL)) {
            logger.warn("get google OAUTH_URL failed!");
            throw new Exception("get google OAUTH_URL failed!");
        }
    }
    public LoginRspDto loginCheck (LoginReqDto loginReqDto,LoginRspDto loginRspDto) throws Exception {
        if  (loginReqDto.getOpenId() == null) {
            logger.warn("google登陆授权验证失败，opendId为空请稍后再试");
            return null;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (logger.isDebugEnabled()) {
            logger.debug("google登陆授权验证idToken:{},authroizeCode:{}",loginReqDto.getIdCode(),loginReqDto.getCode());
        }
        Boolean isRight =GoogleUtils.checkIdToken(loginReqDto.getIdCode(),APPID);
        if (!isRight) {
            logger.warn("google登陆授权验证失败，isRight:{}",isRight);
            return null;
        }
            GoogleUserInfo googleUserInfo =  GoogleUtils.getInfoByCode(loginReqDto.getCode(),OAUTH_URL,APPID,APPSECRET);
            if (googleUserInfo == null ) {
                logger.error("google登陆授权失败，请稍后再试 {}", JSON.toJSON(loginReqDto));
                return null;
            }
            if (StringUtils.isEmpty(loginRspDto.getUid())) {
                loginRspDto.setLocation(loginReqDto.getLocation());
                loginRspDto.setNickName(googleUserInfo.getName());
                loginRspDto.setOpenId(googleUserInfo.getUserId());
                loginRspDto.setAvatar(replaceDefualtAvatar(googleUserInfo.getPictureUrl()));
                loginRspDto.setOpenId(loginReqDto.getOpenId());
                loginRspDto.setLoginType(LoginType.GOOGLE.getType());
                // 获取用户生日和性别
               Map<String,String> userGenderMap =  getUserGenderAndBirtday(googleUserInfo.getAccessToken());
               if (userGenderMap != null) {
                   loginRspDto.setSex(getOverSeaSex(userGenderMap.get("gender")));
                   loginRspDto.setBirthday(userGenderMap.get("birthday"));
                   loginRspDto.setEmail(userGenderMap.get("email"));
               }
            }
            loginRspDto.setAge(getOverSeaAge(loginRspDto.getBirthday()));
            loginRspDto.setAccessToken(googleUserInfo.getAccessToken());
            loginRspDto.setExpires(String.valueOf(googleUserInfo.getExpiresInSeconds()));
            if (logger.isDebugEnabled()) {
                logger.debug("GoogleLoginServcie google api http cost time:{}",stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        return loginRspDto;
    }
    public String replaceDefualtAvatar(String avatar) {
        // 替换默认图片
        if (!StringUtils.isEmpty(avatar)) {
            String  thirdPartDefaultPicture = heraclesConfig.getThirdPartDefaultPicture();
            String userDefaultPricture = heraclesConfig.getUserDefaultPicture();
            if (!StringUtils.isEmpty(userDefaultPricture) && !StringUtils.isEmpty(thirdPartDefaultPicture)) {
                String[] thirdPictures = thirdPartDefaultPicture.split(";");
                for (String thirdPicture : thirdPictures) {
                    if (thirdPicture.equals(avatar)) {
                        avatar = userDefaultPricture;
                        break;
                    }
                }
            }
        }
        return avatar;
    }
    public  Map<String, String>  getUserGenderAndBirtday(String code) {
        try {
            String uriPath = "https://people.googleapis.com/v1/people/me?key=" + APIKEY + "&personFields=genders,birthdays,emailAddresses&sources=READ_SOURCE_TYPE_PROFILE";
            if (logger.isDebugEnabled()){
                logger.debug("google getUserGenderAndBirtday uripath{}:", uriPath);
            }
            Map<String, String> userMap= Maps.newHashMap();
            Map<String, String> headerMap= Maps.newHashMap();
            headerMap.put("Authorization","Bearer  " +code);
            String resultStr = httpGetJsonResult(uriPath,headerMap);
            if (StringUtils.isBlank(resultStr)) {
                return null;
            }
            JSONObject debugResult = JSON.parseObject(resultStr);
            if (debugResult == null || debugResult.get("error") != null) {
                return null;
            }
            userMap.put("gender", parseGenderJSON(debugResult));
            userMap.put("birthday", parseBirthdayJSON(debugResult));
            userMap.put("email", parseEmailJSON(debugResult));
            return userMap;
        } catch (Exception e) {
            logger.error("getUserLongCode facebookapi error,params :{}",  code,e);
        }
        return null;
    }


    private String httpGetJsonResult(String uriPath,Map<String,String> headerMap) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("[GoogleLoginService]: httpGetJsonResult request url={}, params = {}", uriPath, uriPath);
        }
        String returnStr = null;
        try {
            returnStr = httpClient.getReturnString(uriPath,headerMap);
            if (logger.isDebugEnabled()) {
                logger.warn("[GoogleLoginService]:  httpGetJsonResult response  result={}", returnStr);
            }
        } catch (Exception e) {
            logger.error("GoogleLoginService httpGetJsonResult error", e);
        }
        return returnStr;
    }
}



