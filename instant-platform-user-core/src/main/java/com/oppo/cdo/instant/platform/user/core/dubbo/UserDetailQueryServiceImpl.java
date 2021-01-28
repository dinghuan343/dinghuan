package com.oppo.cdo.instant.platform.user.core.dubbo;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oppo.cdo.instant.platform.common.base.util.BaseUtil;
import com.oppo.cdo.instant.platform.im.domain.FollowStatisticDTO;
import com.oppo.cdo.instant.platform.im.facade.FollowService;
import com.oppo.cdo.instant.platform.user.core.service.UserInfoManager;
import com.oppo.cdo.instant.platform.user.core.service.UserManagerService;
import com.oppo.cdo.instant.platform.user.core.service.UserSessionInfoManager;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserAllDTO;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserFollowDTO;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserSessionDTO;
import com.oppo.cdo.instant.platform.user.facade.UserDetailQueryService;

@Service
public class UserDetailQueryServiceImpl implements UserDetailQueryService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailQueryService.class);

    @Autowired
    UserManagerService userManagerService;

    @Autowired
    private UserSessionInfoManager userSessionInfoManager;
    @Autowired
    private UserInfoManager userInfoManager;
    @Autowired
    private FollowService followService;

    @Override
    public UserBasicDTO queryUserBasicByUid(String uid) {
        return userManagerService.getByUid(uid);
    }

    @Override
    public List<UserBasicDTO> listUserBasicInfoByUid(Set<String> uidSet) {
        boolean empty = CollectionUtils.isEmpty(uidSet);
        if (empty || uidSet.size() > 20) {
            int size = empty ? 0 : uidSet.size();
            throw new RuntimeException("uidSet is null or length is larger than 20,size = "+ size);
        }
        return userInfoManager.listByUid(uidSet);

    }

    @Override
    public UserSessionDTO queryUserSessionByUid(String uid) {
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        return userSessionInfoManager.getUserSessionInfo(uid);
    }

    @Override
    public UserFollowDTO queryUserFollowByUid(String uid) {
        UserFollowDTO uf = new UserFollowDTO();
        uf.setUid(uid);
        List<FollowStatisticDTO> followInfo;
        try {
            followInfo = followService.listFollowStatisticInfo(BaseUtil.createSet(uid));
        } catch (Exception e) {
            logger.warn("getFollowInfo error!", e);
            followInfo = null;
        }
        if (org.springframework.util.CollectionUtils.isEmpty(followInfo)) {
            uf.setFollowEachOtherCount(0);
            uf.setFollowerCount(0);
            uf.setFollowingCount(0);
        } else {
            uf.setFollowEachOtherCount(followInfo.get(0).getFollowEachOtherCount());
            uf.setFollowerCount(followInfo.get(0).getFollowerCount());
            uf.setFollowingCount(followInfo.get(0).getFollowingCount());
        }

        return uf;
    }

    @Override
    public UserAllDTO queryUserAllByUid(String uid) {
        UserAllDTO ua = new UserAllDTO();
        UserBasicDTO basicInfoDto = userManagerService.getByUid(uid);

        UserSessionDTO us = userSessionInfoManager.getUserSessionInfo(uid);
        UserFollowDTO uf = queryUserFollowByUid(uid);
        ua.setUserBasicDTO(basicInfoDto);
        ua.setUserFollowDTO(uf);
        ua.setUserSessionDTO(us);
        return ua;
    }

    @Override
    public String querySessionInfoByUid(String uid) {
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        return userSessionInfoManager.getSession(uid);
    }


}
