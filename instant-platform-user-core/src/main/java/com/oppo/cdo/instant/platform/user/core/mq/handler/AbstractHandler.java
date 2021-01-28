package com.oppo.cdo.instant.platform.user.core.mq.handler;

import com.oppo.cdo.instant.platform.common.base.util.SessionInfoUtil;
import com.oppo.cdo.instant.platform.common.domain.ws.WsMsgHeader;
import com.oppo.cdo.instant.platform.user.facade.UserDetailQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2018/12/17 17:51
 */
public class AbstractHandler {
    
    @Autowired
    private UserDetailQueryService userSessionInfoService;
    
    protected String getUid(WsMsgHeader wsMsgHeader) throws IllegalArgumentException {
        if (wsMsgHeader == null) {
            throw new IllegalArgumentException("wsMsgHeader is null");
        }
        
        String uid = (String) wsMsgHeader.getProperties().get("uid");
        if(StringUtils.isEmpty(uid)){
            throw new IllegalArgumentException("uid get by session is null");
        }
        return uid;
    }
}
