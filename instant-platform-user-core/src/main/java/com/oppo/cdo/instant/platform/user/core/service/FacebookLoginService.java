package com.oppo.cdo.instant.platform.user.core.service;

import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.getOverSeaAge;
import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.getOverSeaBirthDay;
import static com.oppo.cdo.instant.platform.user.core.util.OverSeaDataDealUtil.getOverSeaSex;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.domain.dto.req.LoginReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.LoginRspDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicInfoDto;
import com.oppo.cdo.instant.platform.user.domain.type.LoginType;
import com.oppo.framework.okhttp.HttpClient;

@Component
public class FacebookLoginService {

    private static final Logger logger = LoggerFactory.getLogger(FacebookLoginService.class);

    private static String PRE_URL ;

    private static String APPID ;

    private static String APPSECRET ;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private HeraclesConfig heraclesConfig;

    @PostConstruct
    public void initFacebookAppId() throws Exception {
        APPID = heraclesConfig.getFacebookAppId();
        APPSECRET = heraclesConfig.getFacebookSecret();
        PRE_URL = heraclesConfig.getFacebookPRE_URL();
        if (org.apache.commons.lang3.StringUtils.isEmpty(APPID)) {
            logger.warn("get facebook APPID failed!");
            throw new Exception("get google APPID failed!");
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(APPSECRET)) {
            logger.warn("get facebook APPSECRET failed!");
            throw new Exception("get facebook APPSECRET failed!");
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(PRE_URL)) {
            logger.warn("get facebook OAUTH_URL failed!");
            throw new Exception("get facebook OAUTH_URL failed!");
        }
    }

    public LoginRspDto loginCheck(LoginReqDto loginReqDto, LoginRspDto loginRspDto) throws Exception {
        if (loginReqDto.getOpenId() == null) {
            logger.error("facebook登陆授权验证失败，opendId为空请稍后再试");
            return null;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();

        Map<String, String> accessTokenMap = getUserLongCode(loginReqDto.getCode());
        if (accessTokenMap == null) {
            logger.error("facebook刷新长期token失败，请稍后再试 {}", JSON.toJSON(loginReqDto));
            return null;
        }
        String accessToken = accessTokenMap.get("access_token");
        // 第一次登陆设置expires有效期为50天，去掉debug_token验证
        String expires = String.valueOf((System.currentTimeMillis()/1000) + heraclesConfig.getAccessTokenExpire());
        if ( !StringUtils.isEmpty(loginRspDto.getUid())) {
            // 调取验证tokenapi
            JSONObject isValid = debugToken(accessToken);
            if (isValid == null || isValid.get("is_valid") == null ||  "false".equals(isValid.get("is_valid").toString())) {
                logger.error("facebook登陆授权验证失败，请稍后再试 isValid:{}", isValid);
                return null;
            }
            if( isValid.get("user_id") != null && !ObjectUtils.toString(isValid.get("user_id")).equals(loginReqDto.getOpenId())) {
                logger.error("facebook登陆授权验证失败，openId is not equals, isValid:{}", isValid);
                return null;
            }
             expires = ObjectUtils.toString(isValid.get("expires_at"));
        }



        if (StringUtils.isEmpty(loginRspDto.getUid())) {
            // 获取用户基本信息
            UserBasicInfoDto basicUserEntity = getUserInfoApi(accessToken);
            if (basicUserEntity == null) {
                logger.error("facebook登陆授权获取用户信息失败，请稍后再试 {}", JSON.toJSON(loginReqDto));
                return null;
            }
            // 获取用户头像
            basicUserEntity.setAccessToken(accessToken);
            loginRspDto.setAvatar(basicUserEntity.getAvatar());
            if (StringUtils.isEmpty(basicUserEntity.getAvatar())) {
                String picture = getUserPhotoApi(basicUserEntity);
                loginRspDto.setAvatar(picture);
            }
            loginRspDto.setAddress(basicUserEntity.getAddress());
            loginRspDto.setSex(getOverSeaSex(basicUserEntity.getSex()));
            loginRspDto.setEmail(basicUserEntity.getEmail());
            loginRspDto.setNickName(basicUserEntity.getNickName());
            loginRspDto.setOpenId(basicUserEntity.getOpenId());
            loginRspDto.setBirthday(getOverSeaBirthDay(basicUserEntity.getBirthday()));
            loginRspDto.setAge(getOverSeaAge(loginRspDto.getBirthday()));
            loginRspDto.setLocation(loginReqDto.getLocation());
        }
        loginRspDto.setAccessToken(accessToken);
        loginRspDto.setLoginType(LoginType.FACEBOOK.getType());
        loginRspDto.setExpires(expires);
        logger.warn("FacebookLoginService facebook api http cost time:{}",stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return loginRspDto;
    }


    /**
     *  获取用户头像
     */
    private String getUserPhotoApi(UserBasicInfoDto userRegisterReqDto) {
        try {
            String uriPath =PRE_URL+"/"+ userRegisterReqDto.getOpenId()+"/picture?redirect=false&access_token="+userRegisterReqDto.getAccessToken();
            JSONObject debugResult = getHttpClinetResult(uriPath);
            if (debugResult == null || debugResult.get("data") == null) {
                return null;
            }
            JSONObject data = (JSONObject) debugResult.get("data");
            String url =  (String) data.get("url");
            String isSilhouette = ObjectUtils.toString(data.get("is_silhouette"));
            if (!StringUtils.isEmpty(isSilhouette) && "true".equals(isSilhouette)) {
                url = replaceDefualtAvatar(url);
            }
            return url;
        } catch (Exception e) {
            logger.error("getUserPhotoApi facebookapi error,params :{}", JSON.toJSON(userRegisterReqDto),e);
        }
        return null;
    }

    /**
     * 获取用户基本信息
     */
    private UserBasicInfoDto getUserInfoApi(String accessToken) {
        try {
            String uripath =PRE_URL+"/me?fields=picture.width(720).height(720),birthday,name,gender,location,email&access_token="+accessToken;
            JSONObject debugResult = getHttpClinetResult( uripath);
            if (debugResult == null || debugResult.get("id") == null) {
                return null;
            }
            UserBasicInfoDto userRegisterReqDto = new UserBasicInfoDto();
            userRegisterReqDto.setOpenId(ObjectUtils.toString(debugResult.get("id")));
            userRegisterReqDto.setNickName(ObjectUtils.toString(debugResult.get("name")));
            userRegisterReqDto.setSex(ObjectUtils.toString(debugResult.get("gender")));
            userRegisterReqDto.setBirthday(ObjectUtils.toString(debugResult.get("birthday")));
            userRegisterReqDto.setEmail(ObjectUtils.toString(debugResult.get("email")));
            if (debugResult.get("picture") != null) {
                JSONObject picture = (JSONObject) debugResult.get("picture");
                if (picture != null && picture.get("data") != null) {
                    JSONObject data = (JSONObject) picture.get("data");
                    String url = ObjectUtils.toString(data.get("url"));
                    String isSilhouette = ObjectUtils.toString(data.get("is_silhouette"));
                    if (!StringUtils.isEmpty(isSilhouette) && "true".equals(isSilhouette)) {
                        url = replaceDefualtAvatar(url);
                    }
                    userRegisterReqDto.setAvatar(url);
                }
            }
            if (debugResult.get("location") != null) {
                JSONObject data = (JSONObject) debugResult.get("location");
                if (data != null && data.get("name") != null) {
                    String address = ObjectUtils.toString(data.get("name"));
                    userRegisterReqDto.setAddress(address);
                }
            }
            return userRegisterReqDto;
        } catch (Exception e) {
            logger.error("getUserInfoApi facebookapi error,params :{}", accessToken,e);
        }
        return null;
    }

    /**
     * 验证客户端token
     */
    private JSONObject debugToken(String code) {
        try {
            String uriPath = PRE_URL + "/v8.0/debug_token?access_token=" + APPID + "%7C" + APPSECRET + "&input_token="
                    + code;
            if (logger.isDebugEnabled()) {
                logger.debug("facebook check token uripath{}:", uriPath);
            }
            JSONObject debugResult = getHttpClinetResult(uriPath);
            if (debugResult != null && debugResult.get("data") != null) {
                JSONObject data = (JSONObject) debugResult.get("data");
                return data;
            }
            return null;
        } catch (Exception e) {
            logger.error("facebook loginCheck false: please check url,params :{}", JSON.toJSON(code),e);
        }
        return null;
    }

    /**
     * 刷新用户token
     */
    private Map<String, String> getUserLongCode(String code) {
        try {
            String uriPath = PRE_URL + "/oauth/access_token?client_id=" + APPID + "&client_secret=" + APPSECRET
                    + "&grant_type=fb_exchange_token&fb_exchange_token=" + code;
            logger.warn("facebook getUserLongCode uripath{}:", uriPath);
            Map<String, String> userMap = Maps.newHashMap();
            JSONObject debugResult = getHttpClinetResult(uriPath);
            if (debugResult == null || debugResult.get("access_token") == null) {
                return null;
            }
            userMap.put("access_token", (String) debugResult.get("access_token"));
            userMap.put("expires_in", (String) debugResult.get("expires_in").toString());
            return userMap;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getUserLongCode facebookapi error,params :{}",  code,e);
        }
        return null;
    }

    private JSONObject getHttpClinetResult (String uriPath) throws Exception{
                Stopwatch stopwatch = Stopwatch.createStarted();
                String httpReturnStr = httpGetJsonResult(uriPath);
                if (logger.isDebugEnabled()) {
                    logger.warn("debugToken result is {}, handleTime={}", httpReturnStr,  stopwatch.elapsed(TimeUnit.MILLISECONDS));
                }
                return JSON.parseObject(httpReturnStr);
    }

    private String httpGetJsonResult(String uriPath) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("[FACEBOOK]: httpGetJsonResult request url={}, params = {}", uriPath, uriPath);
        }
        String returnStr = null;
        try {
            returnStr = httpClient.getReturnString(uriPath);
        } catch (Exception e) {
            logger.error("FacebookLoginService httpGetJsonResult error", e);
        }
        logger.debug("[FacebookLoginService]:  httpGetJsonResult response  result={}", returnStr);

        return returnStr;
    }
    private String replaceDefualtAvatar(String avatar) {
        // 替换默认图片
        if (!StringUtils.isEmpty(avatar)) {
            String userDefaultPricture = heraclesConfig.getUserDefaultPicture();
            if (!StringUtils.isEmpty(userDefaultPricture)) {
                avatar = userDefaultPricture;
            }
        }
        return avatar;
    }
}
