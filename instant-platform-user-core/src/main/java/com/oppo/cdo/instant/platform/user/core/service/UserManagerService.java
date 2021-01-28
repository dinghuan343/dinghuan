package com.oppo.cdo.instant.platform.user.core.service;

import com.oppo.cdo.instant.platform.common.sensitive.SensitiveTextInfo;
import com.oppo.cdo.instant.platform.common.sensitive.SensitiveUtils;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesAIUsersConfig;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.core.config.SensitiveConfig;
import com.oppo.cdo.instant.platform.user.core.db.service.UserDbService;
import com.oppo.cdo.instant.platform.user.core.util.AIUtil;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UpdateUserInfoReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UserRegisterReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.LoginRspDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UpdateUserResult;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.entity.UserOauthInfoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static com.oppo.cdo.instant.platform.user.core.util.BeanToLoginRspDto.convertEntityToRsp;
import static com.oppo.cdo.instant.platform.user.core.util.UserTextSensitiveUtil.getNickNameBizId;
import static com.oppo.cdo.instant.platform.user.core.util.UserTextSensitiveUtil.getUserSignBizId;

@Component
public class UserManagerService {

    private static final Logger   logger = LoggerFactory.getLogger(UserManagerService.class);

    @Autowired
    private UserDbService         userDbService;
    @Autowired
    private UserInfoManager       userInfoManager;
    @Autowired
    private HeraclesConfig        heraclesConfig;
    @Autowired
    private HeraclesAIUsersConfig heraclesAIUsersConfig;
    @Autowired
    private SensitiveConfig       sensitiveConfig;

    public LoginRspDto queryUserInfo(String openId, Integer loginType) {
        UserOauthInfoEntity userOAuthInfo = userInfoManager.getUserOAuthInfoByOpenId(openId, loginType);
        if (null == userOAuthInfo) {
            return new LoginRspDto();
        }
        UserBasicDTO userBasicInfo = userInfoManager.getUserBasicInfo(userOAuthInfo.getUid());

        return convertEntityToRsp(userBasicInfo, userOAuthInfo);
    }

    public void registerFormalUser(UserRegisterReqDto userRegisterReqDto) throws Exception {
        logger.debug("start get registerFormalUser from db param:{}", userRegisterReqDto);
        userDbService.addUser(userRegisterReqDto);
    }

    public UserBasicDTO getByUid(String uid) {
        if (StringUtils.isEmpty(uid)) {
            logger.warn("getByUid param uid is emtpy,uid={}",uid);
            return null;
        }
        // 不是机器人
        if (!AIUtil.isAI(uid)) {
            return userInfoManager.getUserBasicInfo(uid);
        }
        return heraclesAIUsersConfig.getAIByUid(uid);
    }

    public UpdateUserResult updateUserInfo(UpdateUserInfoReqDto reqDto) {
        if (logger.isDebugEnabled()) {
            logger.debug("start updateUserInfo from  by uid:{}", reqDto);
        }
        if (reqDto == null || reqDto.getUid() == null) {
            logger.error("updateUserInfo failed ,params empty uid", reqDto);
            return UpdateUserResult.fail(false);
        }
        try {
            // 检测昵称敏感词
            if (!StringUtils.isEmpty(reqDto.getNickName()) && heraclesConfig.getUserNickeNameCheck()) {
                SensitiveTextInfo sensitiveTextInfo = sensitiveConfig.getTextSensitive();
                sensitiveTextInfo.setData(reqDto.getNickName());
                sensitiveTextInfo.setBizId(getNickNameBizId(reqDto.getNickName(),reqDto.getUid()));
                Boolean isSafe = SensitiveUtils.textCheck(sensitiveTextInfo);
                if (!isSafe) {
                    return UpdateUserResult.nickNameSensitive();
                }
            }
            // 检测用户签名
            if (!StringUtils.isEmpty(reqDto.getUserSign()) && heraclesConfig.getUserSignCheck()) {
                SensitiveTextInfo sensitiveTextInfo = sensitiveConfig.getTextSensitive();
                sensitiveTextInfo.setData(reqDto.getUserSign());
                sensitiveTextInfo.setBizId(getUserSignBizId(reqDto.getUserSign(),reqDto.getUid()));
                Boolean isSafe = SensitiveUtils.textCheck(sensitiveTextInfo);
                if (!isSafe) {
                    return UpdateUserResult.userSignSensitive();
                }
            }
            // 头像检测(配置开关)
            if (!StringUtils.isEmpty(reqDto.getAvatar()) && heraclesConfig.getUserAvatarCheck()) {
                SensitiveTextInfo sensitiveImage = sensitiveConfig.getImageSensitive();
                sensitiveImage.setData(reqDto.getAvatar());
                sensitiveImage.setBizId(UUID.randomUUID().toString());
                Boolean isSafe = SensitiveUtils.imageCheck(sensitiveImage);
                if (!isSafe) {
                    return UpdateUserResult.userAvatarSensitive();
                }
            }
            userDbService.updateUserInfo(reqDto);
        } catch (Exception e) {
            logger.error("updateUserAfterSensitiveCheck error,updateUserInfoReqDto:{}", reqDto, e);
            return UpdateUserResult.fail(false);
        }
        return UpdateUserResult.success(true);
    }

    public void refreshUserAccessToken(UpdateUserInfoReqDto updateUserInfoReqDto) throws Exception {
        userDbService.refreshUserAccessToken(updateUserInfoReqDto);
    }


    public Boolean updateNickeNameSensitiveWord(String uid) {
        UpdateUserInfoReqDto updateUserInfoReqDto = new UpdateUserInfoReqDto();
        updateUserInfoReqDto.setUid(uid);
        updateUserInfoReqDto.setNickName(heraclesConfig.getSensitiveWordText());
        userDbService.updateUserInfo(updateUserInfoReqDto);
        return true;
    }

    public Boolean updateUserSignSensitiveWord(String uid) {
        UpdateUserInfoReqDto updateUserInfoReqDto = new UpdateUserInfoReqDto();
        updateUserInfoReqDto.setUid(uid);
        updateUserInfoReqDto.setUserSign(heraclesConfig.getUserSignWordText());
        userDbService.updateUserInfo(updateUserInfoReqDto);
        return true;
    }

}
