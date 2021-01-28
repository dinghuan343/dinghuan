package com.oppo.cdo.instant.platform.user.core.dubbo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oppo.cdo.instant.platform.user.core.cache.SessionInfoMapBuilder;
import com.oppo.cdo.instant.platform.user.core.service.UserSessionInfoManager;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.core.util.AIUtil;
import com.oppo.cdo.instant.platform.user.facade.UserDetailUpdateService;

@Service
public class UserDetailUpdateServiceImpl implements UserDetailUpdateService {

    @Autowired
    private UserSessionInfoManager userSessionInfoManager;
    @Autowired
    private HeraclesConfig       heraclesConfig;

    @Override
    public int updatePlayStatus(String uid, String status) {
        SessionInfoMapBuilder builder = new SessionInfoMapBuilder();
        builder.putPlayStatus(status);
        // 不是机器人改状态
        if (!AIUtil.isAI(uid)) {
            userSessionInfoManager.cacheUserSessionInfo(uid, builder);
        }
        return 1;
    }

}
