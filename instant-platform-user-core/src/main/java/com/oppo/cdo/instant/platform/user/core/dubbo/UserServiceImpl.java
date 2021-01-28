package com.oppo.cdo.instant.platform.user.core.dubbo;

import com.oppo.cdo.instant.platform.common.domain.base.ResultCode;
import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.user.core.service.PlatTokenManagerService;
import com.oppo.cdo.instant.platform.user.core.service.UserManagerService;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UpdateUserInfoReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UpdateUserResult;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UpdateUserResultCode;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserTextInspectDto;
import com.oppo.cdo.instant.platform.user.facade.UserInfoService;
import com.oppo.instant.game.web.proto.common.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.oppo.cdo.instant.platform.user.core.util.UserTextSensitiveUtil.*;

@Service
public class UserServiceImpl implements UserInfoService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserManagerService  userManagerService;
    @Autowired
    private PlatTokenManagerService platTokenManagerService;
    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;

    @Override
    public UpdateUserResult updateFormalUser(UpdateUserInfoReqDto reqDto) {
        if (reqDto == null) {
            return UpdateUserResult.fail(false);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("updateUserInfo start, reqDto:{}", reqDto);
        }
        try {
            reqDto.setLocation(null);
            return userManagerService.updateUserInfo(reqDto);
        } catch (Exception e) {
            logger.error("[userManagerService] updateFormalUser error,reqDto{}", reqDto, e);
            return UpdateUserResult.fail(false);
        }
    }

    @Override
    public String queryUidByAid(String aid) {
        try {
            return platTokenManagerService.getUidByAid(aid);
        } catch (Exception e) {
            logger.error("[platTokenManagerService] queryUidByOid get user empty,oid{}:", aid, e);
            return null;
        }
    }

    @Override
    public UpdateUserResult textSensitiveCallBack(UserTextInspectDto userTextInspectDto) {
        if (logger.isDebugEnabled()) {
            logger.debug("textSensitiveCallBack begin,userTextInspectDto={}",userTextInspectDto);
        }
        // 检测到风险文字替换掉
        String bizId = userTextInspectDto.getBizId();
        String uid = getUidByBizId(userTextInspectDto.getBizId());
        // 是否需要替换（旧的数据不处理）
        if ( !isNeedInspectUser(bizId) ) {
            logger.warn("textSensitiveCallBack failed,bizId is not need deal with ,userTextInspectDto={},userBasicDTO={}",userTextInspectDto);
            return UpdateUserResult.fail(false, ResultCode.SYS_ERROR.getCode(),"文字类型不明确不处理");
        }
        UserBasicDTO userBasicDTO  = userManagerService.getByUid(uid);
        if (userBasicDTO == null ) {
            logger.warn("textSensitiveCallBack failed,userBasicDTO is null,userTextInspectDto={},userBasicDTO={}",userTextInspectDto,userBasicDTO);
            return UpdateUserResult.fail(false, ResultCode.SYS_ERROR.getCode(),"文字关联用户不存在");
        }
        Boolean updateResult = false;
        if (isNickeNameText(bizId,userBasicDTO.getNickName())) {
            updateResult = userManagerService.updateNickeNameSensitiveWord(uid);
        }else if (isUserSignText(bizId,userBasicDTO.getUserSign())) {
            updateResult =userManagerService.updateUserSignSensitiveWord(uid);
        }
        if ( !updateResult ){
            logger.warn("textSensitiveCallBack failed,userTextInspectDto={},userBasicDTO={}",userTextInspectDto,userBasicDTO);
            return UpdateUserResult.fail(false, ResultCode.SYS_ERROR.getCode(),"敏感词不存在，无法修改");
        }
        return UpdateUserResult.success(true,ResultCode.SUCCESS.getCode(),"回调成功");
    }


}
