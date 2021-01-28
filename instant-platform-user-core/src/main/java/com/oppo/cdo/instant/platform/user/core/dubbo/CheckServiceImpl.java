package com.oppo.cdo.instant.platform.user.core.dubbo;

import com.google.common.base.Stopwatch;
import com.oppo.cdo.instant.platform.user.core.service.PlatTokenManagerService;
import com.oppo.cdo.instant.platform.user.core.service.UserManagerService;
import com.oppo.cdo.instant.platform.user.domain.dto.req.LoginReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.LoginRspDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.facade.CheckService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.oppo.cdo.instant.platform.user.core.util.BeanToLoginRspDto.covertToLoginRspFromDto;

@Service
public class CheckServiceImpl implements CheckService {

    private static final Logger     logger = LoggerFactory.getLogger(CheckServiceImpl.class);

    @Autowired
    private UserManagerService      userManagerService;
    @Autowired
    private PlatTokenManagerService platTokenManagerService;

    @Override
    public LoginRspDto checkTokenApk(LoginReqDto loginReqDto) {
        if (logger.isDebugEnabled()) {
            logger.debug("checkTokenApk begin,params:{}", loginReqDto);
        }
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            String token = loginReqDto.getToken();
            boolean checkResponse = platTokenManagerService.checkToken(token);
            if (!checkResponse) {
                logger.debug("checkService  checkTokenApk failed ,token:{}", token);
                return null;
            }
            String uid = platTokenManagerService.getUidByToken(token);
            if (StringUtils.isEmpty(uid)) {
                logger.debug("checkService  checkTokenApk failed ,getUidByToken is empty  token:{},uid:{}", loginReqDto,uid);
                return null;
            }
            UserBasicDTO basicInfoDto = userManagerService.getByUid(uid);
            if (null == basicInfoDto) {
                logger.debug("checkService this user:{} isn't exist!", uid);
                return null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(" checkService checkTokenApk end token:{},cost time:{}", token,stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
            return covertToLoginRspFromDto(basicInfoDto);
        } catch (Exception e) {
            logger.error("checkService  checkTokenApk error  token:{}", loginReqDto.getToken(), e);
            return null;
        }
    }

    @Override
    public boolean checkToken(String token) {

        try {
            return platTokenManagerService.checkToken(token);
        } catch (Exception e) {
            logger.error("checkToken error,e=",e);
        }
        return false;
    }

}
