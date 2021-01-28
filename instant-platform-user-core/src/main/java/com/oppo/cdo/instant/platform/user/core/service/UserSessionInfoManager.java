package com.oppo.cdo.instant.platform.user.core.service;

import com.alibaba.fastjson.JSON;
import com.oppo.cdo.instant.platform.common.base.util.ProtostuffUtil;
import com.oppo.cdo.instant.platform.common.core.mq.MqMsgSender;
import com.oppo.cdo.instant.platform.common.domain.type.CommonCode;
import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.user.core.cache.SessionInfoMapBuilder;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.core.util.AIUtil;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserSessionDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.oppo.cdo.instant.platform.user.core.constant.RedisKeyPrefix.USER_SESSION;

/**
 * description:
 *
 * @author ouyangrenyong
 * @since
 */
@Component
public class UserSessionInfoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSessionInfoManager.class);

    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;

    @Autowired
    private HeraclesConfig      heraclesConfig;
    @Autowired
    @Qualifier("platformMqMsgSender")
    private MqMsgSender         mqMsgSender;

    private String getUserSessionKey(String uid) {
        return USER_SESSION + uid;
    }

    /**
     * 缓存用户的session信息，一般用于login事件，其他事件可以找具体的方法处理
     * 
     * @param uid
     * @param sessionInfoMapBuilder value不能为null,该方法只做新增和替换
     * @return
     */
    public boolean cacheUserSessionInfo(String uid, SessionInfoMapBuilder sessionInfoMapBuilder) {
        if (StringUtils.isBlank(uid)) {
            return false;
        }
        String key = getUserSessionKey(uid);
        Map<String, String> sessionMap = sessionInfoMapBuilder.getSessionMap();
        String onlineStatus = sessionMap.get(SessionInfoMapBuilder.ONLINE_STATUS);
        Integer userOnlineCacheTime = heraclesConfig.getUserOnlineCacheTime();
        // 修改了用户在线状态
        if (null != onlineStatus) {
            String cacheOnlineStatus = redisClusterManager.hget(key, SessionInfoMapBuilder.ONLINE_STATUS);
            sendUserStatusChangeEvent(uid, cacheOnlineStatus, onlineStatus);
        }
        try {

            redisClusterManager.hmset(key, sessionMap);
            redisClusterManager.expire(key, userOnlineCacheTime);
            return true;
        } catch (Exception e) {
            LOGGER.error("redis operate failed,key={},value={},userOnlineCacheTime={}", key, JSON.toJSONString(sessionMap),userOnlineCacheTime,e);
            // FIXME: 2020/12/23 还不知道为啥会有这个异常，先补一下
            Set<Map.Entry<String, String>> entries = sessionMap.entrySet();
            for (Map.Entry<String,String> entry : entries) {
                if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())) {
                    redisClusterManager.hset(key,entry.getKey(),entry.getValue());
                }else {
                    LOGGER.error("redis operate failed,key={},entry={}",key,entry);
                }
            }
            redisClusterManager.expire(key, userOnlineCacheTime);
        }
        return false;
    }

    /**
     * 发送更新状态的mq事件
     * 
     * @param newUserStatus 用户老的在线状态
     * @param oldUserStatus 用户新的在线状态
     */
    public boolean sendUserStatusChangeEvent(String uid, String oldUserStatus, String newUserStatus) {
        String userStatus = uid + "_" + newUserStatus;
        try {
            LOGGER.info("send userStatus mq :" + userStatus);
            boolean pushResult = mqMsgSender.sendMsg(ProtostuffUtil.serialize(userStatus),
                                                     heraclesConfig.getMqProperties().getProperty("user.rocketmq.producer.topic"),
                                                     heraclesConfig.getMqProperties().getProperty("user.rocketmq.producer.userOnlineStatusChange.tag"));
            if (!pushResult) {
                LOGGER.warn("pushUserStatusEvent failed! send message to MQ failed! uid:{}", uid);
            }
            return pushResult;
        } catch (Exception e) {
            LOGGER.warn("pushUserStatusEvent failed! uid:{}, exception\n", uid, e);
            return false;
        }
    }

    /**
     * 用户下线
     * 
     * @param uid
     * @return true=成功，false=失败
     */
    public boolean logout(String uid) {
        String userSessionKey = getUserSessionKey(uid);
        String cacheUserStatus = redisClusterManager.hget(userSessionKey, SessionInfoMapBuilder.ONLINE_STATUS);
        LOGGER.info("logout cacheUserStatus={}",cacheUserStatus);
        try {
            redisClusterManager.hset(userSessionKey, SessionInfoMapBuilder.ONLINE_STATUS,
                                     CommonCode.STATUS_OFF_LINE.getCode());
            // redis更新完成后才能发送
            sendUserStatusChangeEvent(uid, cacheUserStatus, CommonCode.STATUS_OFF_LINE.getCode());

            redisClusterManager.hdel(userSessionKey, SessionInfoMapBuilder.DEVICE_ID, SessionInfoMapBuilder.SESSION,
                                     SessionInfoMapBuilder.PLAY_STATUS);
            return true;
        } catch (Exception e) {
            LOGGER.error("logout operate redis failed,uid = {},e={}", uid, e.getCause());
        }
        return false;
    }

    public String getSession(String uid) {
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        if (AIUtil.isAI(uid)) {
            return "0";
        }
        String userSessionKey = getUserSessionKey(uid);
        List<String> sessionInfo = redisClusterManager.hmget(userSessionKey, SessionInfoMapBuilder.SESSION);
        if (CollectionUtils.isNotEmpty(sessionInfo)) {
            return sessionInfo.get(0);
        }
        return null;
    }

    public String getDeviceId(String uid) {
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        String userSessionKey = getUserSessionKey(uid);
        List<String> deviceId = redisClusterManager.hmget(userSessionKey, SessionInfoMapBuilder.DEVICE_ID);
        if (CollectionUtils.isNotEmpty(deviceId)) {
            return deviceId.get(0);
        }
        return null;
    }

    public UserSessionDTO getUserSessionInfo(String uid) {
        UserSessionDTO userSessionDTO = new UserSessionDTO();
        userSessionDTO.setUid(uid);
        if (!AIUtil.isAI(uid)) {
            String userSessionKey = getUserSessionKey(uid);
            Map<String, String> userSessionMap = redisClusterManager.hgetAll(userSessionKey);
            userSessionDTO.setDeviceId(userSessionMap.get(SessionInfoMapBuilder.DEVICE_ID));
            userSessionDTO.setOnlineStatus(userSessionMap.get(SessionInfoMapBuilder.ONLINE_STATUS));
            userSessionDTO.setLocation(userSessionMap.get(SessionInfoMapBuilder.LOCATION));
            String playStatus = userSessionMap.get(SessionInfoMapBuilder.PLAY_STATUS);
            if (CommonCode.STATUS_ON_LINE.getCode().equals(userSessionMap.get(SessionInfoMapBuilder.ONLINE_STATUS))
                && StringUtils.isBlank(playStatus)) {
                playStatus = CommonCode.PLAYER_STATUS_FREE.getCode();
            }
            userSessionDTO.setPlayStatus(playStatus);
            userSessionDTO.setSessionInfo(userSessionMap.get(SessionInfoMapBuilder.SESSION));
        } else {
            userSessionDTO.setRobot(AIUtil.isAI(uid));
            userSessionDTO.setOnlineStatus(CommonCode.STATUS_ON_LINE.getCode());
            userSessionDTO.setDeviceId("0");
            userSessionDTO.setLocation("");
            userSessionDTO.setPlayStatus(CommonCode.PLAYER_STATUS_FREE.getCode());
            userSessionDTO.setSessionInfo("0");
        }
        return userSessionDTO;
    }

}
