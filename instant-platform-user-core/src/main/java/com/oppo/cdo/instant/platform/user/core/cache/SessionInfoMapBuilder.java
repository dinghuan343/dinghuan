package com.oppo.cdo.instant.platform.user.core.cache;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 * description:redis中用户维度的会话信息构造器
 *
 * @author ouyangrenyong
 * @since 1.0
 */
public class SessionInfoMapBuilder {

    public final static String ONLINE_STATUS = "onlineStatus";
    public final static String PLAY_STATUS = "playStatus";
    public final static String LOCATION = "location";
    public final static String SESSION = "session";
    public final static String DEVICE_ID = "deviceId";


    private Map<String,String> sessionMap = Maps.newHashMap();

    public Map<String, String> getSessionMap() {
        return sessionMap;
    }

    public Map<String,String> putOnlineStatus(String onlineStatus){
        if (StringUtils.isBlank(onlineStatus)){
            return sessionMap;
        }
        sessionMap.put(ONLINE_STATUS,onlineStatus);
        return sessionMap;
    }

    public Map<String,String> putPlayStatus(String playStatus){
        if (StringUtils.isBlank(playStatus)) {
            return sessionMap;
        }
        sessionMap.put(PLAY_STATUS,playStatus);
        return sessionMap;
    }

    public Map<String,String> putLocation(String location){
        if (StringUtils.isBlank(location)) {
            return sessionMap;
        }
        sessionMap.put(LOCATION,location);
        return sessionMap;
    }
    public Map<String,String> putSession(String sessionInfo){
        if (StringUtils.isBlank(sessionInfo)) {
            return sessionMap;
        }
        sessionMap.put(SESSION,sessionInfo);
        return sessionMap;
    }

    public Map<String,String> putDeviceId(String deviceId){
        if (StringUtils.isBlank(deviceId)) {
            return sessionMap;
        }
        sessionMap.put(DEVICE_ID,deviceId);
        return sessionMap;
    }
}
