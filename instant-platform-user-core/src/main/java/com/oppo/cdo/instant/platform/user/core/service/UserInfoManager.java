package com.oppo.cdo.instant.platform.user.core.service;

import static com.oppo.cdo.instant.platform.user.domain.common.Constant.KEY_USER_BASIC_INFO;
import static com.oppo.cdo.instant.platform.user.domain.common.Constant.KEY_USER_INSTANT_OPENID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.oppo.cdo.instant.platform.user.core.constant.ThreadPool;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.oppo.cdo.instant.platform.common.cache.annotation.OppoRedisCache;
import com.oppo.cdo.instant.platform.common.redis.manager.ProtostuffUtil;
import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.core.util.CacheKeyUtil;
import com.oppo.cdo.instant.platform.user.core.util.UserBeanConvertor;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.entity.UserBaseInfoEntity;
import com.oppo.cdo.instant.platform.user.entity.UserOauthInfoEntity;
import com.oppo.cdo.instant.platform.user.mapper.UserBaseInfoMapper;
import com.oppo.cdo.instant.platform.user.mapper.UserOauthInfoMapper;
import com.oppo.framework.cache0.CacheEntity;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2019/3/7 15:37
 */
@Component
public class UserInfoManager {

    private static final Logger LOGGER         = LoggerFactory.getLogger(UserInfoManager.class);
    // private ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
    // Runtime.getRuntime().availableProcessors() * 4, 60, TimeUnit.SECONDS,
    // new LinkedBlockingQueue<>(1000));

    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;
    @Autowired
    private HeraclesConfig      heraclesConfig;
    @Autowired
    private UserOauthInfoMapper userOauthInfoMapper;
    @Autowired
    private UserBaseInfoMapper  userBaseInfoMapper;

    // 俩周时间,
    private final int           EXPIRE_SECONDS = 604800;

    public void cacheUserBasicInfo(UserBasicDTO basicInfoDto) {
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("cacheUserBasicInfo start:basicInfoDto={}",basicInfoDto);
        }
        if (null == basicInfoDto) {
            return;
        }
        try {
            String cacheKey = CacheKeyUtil.getUserBasicCacheKey(basicInfoDto.getUid());
            CacheEntity entity = new CacheEntity(basicInfoDto);
            entity.setTs(System.currentTimeMillis());
            redisClusterManager.setObject(cacheKey, entity);
            redisClusterManager.expire(cacheKey, EXPIRE_SECONDS);
        } catch (Exception e) {
            LOGGER.warn("cacheUserBasicInfo fail! basicInfoDto:{}, exception", basicInfoDto, e);
        }
    }

    @OppoRedisCache(value = KEY_USER_BASIC_INFO, key = "{0}", asynProcess = true, expireMills = EXPIRE_SECONDS
                                                                                                * 1000, asynExpireMills = 600
                                                                                                                          * 1000)
    public UserBasicDTO getUserBasicInfo(String uid) {
        if (StringUtils.isEmpty(uid)) {
            return null;
        }
        UserBaseInfoEntity userBaseInfoEntity = new UserBaseInfoEntity();
        userBaseInfoEntity.setUid(uid);
        try {
            UserBaseInfoEntity baseInfoEntity = userBaseInfoMapper.queryUserBaseInfo(userBaseInfoEntity);
            return UserBeanConvertor.convert2UserBasicDTO(baseInfoEntity);
        } catch (Exception e) {
            LOGGER.error("getUserBasicInfo {}", uid, e);
            throw new RuntimeException("sys error");
        }
    }

    public List<UserBasicDTO> listByUid(Set<String> uidSet) {
        if (StringUtils.isEmpty(uidSet)) {
            return Collections.emptyList();
        }

        byte[][] uidRedisKeyArray = new byte[uidSet.size()][];
        int index = 0;
        for (String uid : uidSet) {
            uidRedisKeyArray[index++] = CacheKeyUtil.getUserBasicCacheKey(uid).getBytes();
        }

        List<UserBasicDTO> result = new ArrayList<>(uidSet.size());
        List<byte[]> cacheValueList = redisClusterManager.getJedisCluster().mget(uidRedisKeyArray);
        Set<String> cachedUidSet = new HashSet<>(uidSet.size());
        for (byte[] cacheValue : cacheValueList) {
            if (null == cacheValue || cacheValue.length == 0) {
                continue;
            }
            CacheEntity deserialize = ProtostuffUtil.deserialize(cacheValue, CacheEntity.class);
            UserBasicDTO userBasicInfoDto = (UserBasicDTO) deserialize.get();
            if (null == userBasicInfoDto) {
                continue;
            }
            result.add(userBasicInfoDto);
            cachedUidSet.add(userBasicInfoDto.getUid());
        }

        uidSet.removeAll(cachedUidSet);
        if (uidSet.size() <= 0) {
            return result;
        }
        ArrayList<String> uidList = new ArrayList<>(uidSet);
        List<UserBaseInfoEntity> userBaseInfoEntities = userBaseInfoMapper.queryUserBaseInfos(uidList);
        if (CollectionUtils.isEmpty(userBaseInfoEntities)) {
            return result;
        }
        List<UserBasicDTO> userBasicDTOS = userBaseInfoEntities.stream().map(UserBeanConvertor::convert2UserBasicDTO).collect(Collectors.toList());
        result.addAll(userBasicDTOS);
        ThreadPool.commonSubmit(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            for (UserBasicDTO userBasicDTO : userBasicDTOS) {
                String cacheKey = CacheKeyUtil.getUserBasicCacheKey(userBasicDTO.getUid());
                CacheEntity entity = new CacheEntity(userBasicDTO);
                entity.setTs(currentTimeMillis);
                redisClusterManager.setObjectEx(cacheKey, EXPIRE_SECONDS, entity);
            }
        });

        return result;

    }

    @OppoRedisCache(value = KEY_USER_INSTANT_OPENID, key = "{0}_{1}", expireMills = EXPIRE_SECONDS
                                                                                    * 1000, asynProcess = true, asynExpireMills = 600
                                                                                                                                  * 1000)
    public UserOauthInfoEntity getUserOAuthInfoByOpenId(String openId, Integer loginType) {
        UserOauthInfoEntity userQueryEntity = new UserOauthInfoEntity();
        userQueryEntity.setOpenId(openId);
        userQueryEntity.setLoginType(loginType);
        try {
            return userOauthInfoMapper.queryUserOauthInfo(userQueryEntity);
        } catch (Exception e) {
            LOGGER.error("userOauthInfoMapper.queryUserOauthInfo error,e={}", e.getMessage());
            throw e;
        }
    }

    public void cacheUserOAuthInfo(UserOauthInfoEntity oauthInfo) {
        try {
            LOGGER.debug("cacheUserOAuthInfo oauthInfo={}", oauthInfo);
            String cacheOpenIdKey = CacheKeyUtil.getUserBasicCacheOpenIdKey(oauthInfo.getOpenId(),
                                                                            String.valueOf(oauthInfo.getLoginType()));
            CacheEntity entity = new CacheEntity(oauthInfo);
            entity.setTs(System.currentTimeMillis());
            // 缓存openid-loginRspDto

            redisClusterManager.setObject(cacheOpenIdKey, entity);
            redisClusterManager.expire(cacheOpenIdKey, EXPIRE_SECONDS);
        } catch (Exception e) {
            LOGGER.warn("cacheUserOAuthInfo fail! basicInfoDto:{}, exception:{}", oauthInfo, e);
        }
    }

}
