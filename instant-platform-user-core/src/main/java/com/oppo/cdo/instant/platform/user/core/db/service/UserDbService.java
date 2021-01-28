package com.oppo.cdo.instant.platform.user.core.db.service;

import static com.oppo.cdo.instant.platform.user.core.util.BeanToLoginRspDto.convertUserOauthEntity;
import static com.oppo.cdo.instant.platform.user.core.util.BeanToLoginRspDto.convertUserRegisterToBaseEntity;
import static com.oppo.cdo.instant.platform.user.core.util.BeanToLoginRspDto.convertUserUpdateToBaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.user.core.constant.ThreadPool;
import com.oppo.cdo.instant.platform.user.core.service.UserInfoManager;
import com.oppo.cdo.instant.platform.user.core.util.UserBeanConvertor;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UpdateUserInfoReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UserRegisterReqDto;
import com.oppo.cdo.instant.platform.user.entity.UserBaseInfoEntity;
import com.oppo.cdo.instant.platform.user.entity.UserOauthInfoEntity;
import com.oppo.cdo.instant.platform.user.mapper.UserBaseInfoMapper;
import com.oppo.cdo.instant.platform.user.mapper.UserOauthInfoMapper;

@Service
public class UserDbService {

    @Autowired
    private UserBaseInfoMapper  userBaseInfoMapper;
    @Autowired
    private UserOauthInfoMapper userOauthInfoMapper;
    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;
    @Autowired
    private UserInfoManager     userInfoManager;

    private static final Logger logger = LoggerFactory.getLogger(UserDbService.class);

    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserRegisterReqDto userRegisterReqDto) {
        UserBaseInfoEntity userBaseInfoEntity = convertUserRegisterToBaseEntity(userRegisterReqDto);
        UserOauthInfoEntity userOauthInfoEntity = convertUserOauthEntity(userRegisterReqDto);
        userBaseInfoMapper.insert(userBaseInfoEntity);
        userOauthInfoMapper.insert(userOauthInfoEntity);
        // 用户信息放入缓存
        userBaseInfoEntity.setLoginType(userOauthInfoEntity.getLoginType());
        userBaseInfoEntity.setAccessToken(userOauthInfoEntity.getAccessToken());
        userBaseInfoEntity.setExpires(userOauthInfoEntity.getExpires());

        ThreadPool.commonSubmit(() -> {
            logger.debug("addUser oauthInfo={}", userBaseInfoEntity);
            userInfoManager.cacheUserBasicInfo(UserBeanConvertor.convert2UserBasicDTO(userBaseInfoEntity));
            userInfoManager.cacheUserOAuthInfo(userOauthInfoEntity);
        });
    }

    public UserBaseInfoEntity findByUid(String uid) {
        if (StringUtils.isEmpty(uid)) {
            logger.warn("userDbService findByUid error,uid is empty,uid:{}", uid);
            return null;
        }
        UserBaseInfoEntity userQueryEntity = new UserBaseInfoEntity();
        userQueryEntity.setUid(uid);
        return userBaseInfoMapper.queryUserBaseInfo(userQueryEntity);
    }

    public int updateUserInfo(UpdateUserInfoReqDto reqDto) {
        UserBaseInfoEntity userBaseInfoEntity = convertUserUpdateToBaseEntity(reqDto);
        int result = userBaseInfoMapper.updateUserBaseInfo(userBaseInfoEntity);
        // 刷新缓存
        ThreadPool.commonSubmit(new Runnable() {
            @Override
            public void run() {
                try{
                    UserBaseInfoEntity updateAfterEntity = findByUid(reqDto.getUid());
                    if (logger.isDebugEnabled()){
                        logger.debug("updateUserInfo cacheUserInfo sync start,updateAfterEntity={}",updateAfterEntity);
                    }
                    userInfoManager.cacheUserBasicInfo(UserBeanConvertor.convert2UserBasicDTO(updateAfterEntity));
                }catch (Exception e){
                    logger.error("updateUserInfo cacheUserInfo error,userBaseInfoEntity={}",userBaseInfoEntity,e);
                }

            }
        });
        return result;
    }

    public void refreshUserAccessToken(UpdateUserInfoReqDto updateUserInfoReqDto) {
        UserOauthInfoEntity entity = new UserOauthInfoEntity();
        entity.setOpenId(updateUserInfoReqDto.getOpenId());
        entity.setLoginType(updateUserInfoReqDto.getLoginType());
        entity.setAccessToken(updateUserInfoReqDto.getAccessToken());
        entity.setExpires(updateUserInfoReqDto.getExpires());
        userOauthInfoMapper.updateUserOauthInfo(entity);

        UserOauthInfoEntity oauthInfoEntity = userInfoManager.getUserOAuthInfoByOpenId(updateUserInfoReqDto.getOpenId(),
                                                                                       updateUserInfoReqDto.getLoginType());
        if (oauthInfoEntity != null) {
            oauthInfoEntity.setAccessToken(updateUserInfoReqDto.getAccessToken());
            oauthInfoEntity.setExpires(updateUserInfoReqDto.getExpires());
            userInfoManager.cacheUserOAuthInfo(oauthInfoEntity);
        }

    }
}
