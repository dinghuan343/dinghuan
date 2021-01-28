package com.oppo.cdo.instant.platform.user.core.dubbo;

import com.google.common.base.Stopwatch;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesAIUsersConfig;
import com.oppo.cdo.instant.platform.user.core.constant.ThreadPool;
import com.oppo.cdo.instant.platform.user.core.service.FacebookLoginService;
import com.oppo.cdo.instant.platform.user.core.service.GoogleLoginService;
import com.oppo.cdo.instant.platform.user.core.service.PlatTokenManagerService;
import com.oppo.cdo.instant.platform.user.core.service.UserManagerService;
import com.oppo.cdo.instant.platform.user.domain.common.Constant;
import com.oppo.cdo.instant.platform.user.domain.dto.req.LoginReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.req.RepeatLoginReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UpdateUserInfoReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UserRegisterReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.LoginRspDto;
import com.oppo.cdo.instant.platform.user.facade.LoginInService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

import static com.oppo.cdo.instant.platform.user.core.util.BeanToLoginRspDto.constructUserRegisterReqDto;

@Service
public class LoginServiceImpl implements LoginInService {

    private static final Logger     logger                = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private UserManagerService      userManagerService;
    @Autowired
    private FacebookLoginService    facebookLoginService;
    @Autowired
    private GoogleLoginService      googleLoginService;
    @Autowired
    private PlatTokenManagerService platTokenManagerService;

    private final static long       ONE_MONTH_MILLSECONDS = 30 * 24 * 60 * 60 * 1000L;

    @Override
    public LoginRspDto loginIn(LoginReqDto loginReqDto) {
        if (logger.isDebugEnabled()) {
            logger.debug("loginIn begin,params:{}", loginReqDto);
        }
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            LoginRspDto loginRspDto = userManagerService.queryUserInfo(loginReqDto.getOpenId(),
                                                                       loginReqDto.getLoginType());
            if (logger.isDebugEnabled()) {
                logger.debug(" login queryUserInfo end  openid:{},uid:{},cost time:{}", loginReqDto.getOpenId(),
                            loginRspDto.getUid(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
            // 是否需要验证第三方
            Boolean isNeedCheck = checkAccessTokenExpires(loginRspDto);
            if (loginReqDto.getLoginType() == 1 && isNeedCheck) {
                loginRspDto = facebookLoginService.loginCheck(loginReqDto, loginRspDto);
            } else if (loginReqDto.getLoginType() == 2) {
                loginRspDto = googleLoginService.loginCheck(loginReqDto, loginRspDto);
            }
            // 第三方授权异常
            if (loginRspDto == null) {
                logger.error("登陆授权异常，请稍后再试 empty {}", loginReqDto);
                return null;
            }
            if (!StringUtils.isEmpty(loginRspDto.getUid())) {
                return refreshUserTokenByLoginRspDto(loginReqDto, stopwatch, loginRspDto, isNeedCheck);
            }
            return registerUserByLoginRspDto(loginReqDto, stopwatch, loginRspDto);
        } catch (Exception e) {
            logger.error("登陆授权异常，请稍后再试 {}", loginReqDto, e);
            return null;
        }

    }

    private Boolean checkAccessTokenExpires(LoginRspDto loginRspDto) {
        String expires = loginRspDto.getExpires();
        Boolean isNeedCheck = true;
        if (!StringUtils.isEmpty(expires)) {
            try {
                long expiresTime = Long.parseLong(expires);
                long nowTimestamp = System.currentTimeMillis();
                if (expiresTime * 1000 > nowTimestamp + ONE_MONTH_MILLSECONDS) {
                    isNeedCheck = false;
                }
                if (logger.isDebugEnabled()){
                    logger.debug("checkAccessTokenExpires end ,isNeedCheck:{},expiresTime:{}", isNeedCheck, expiresTime);
                }
            } catch (Exception e) {
                logger.error("checkAccessTokenExpires error ,loginRspDto:{}", loginRspDto);
            }

        }
        return isNeedCheck;
    }

    @Override
    public LoginRspDto repeatedLogin(LoginReqDto loginReqDto) {
        if (logger.isDebugEnabled()) {
            logger.debug("repeatedLogin begin,params:{}", loginReqDto);
        }
        loginReqDto.setIsRepeatedLogin(true);
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            LoginRspDto loginRspDto = userManagerService.queryUserInfo(loginReqDto.getOpenId(),
                                                                       loginReqDto.getLoginType());
            if (logger.isDebugEnabled()) {
                logger.debug(" repeatedLogin queryUserInfo end openid:{},uid:{},cost time:{}", loginReqDto.getOpenId(),
                            loginRspDto.getUid(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
            // 第一次注册需要验证第三方
            Boolean isNeedCheck = checkAccessTokenExpires(loginRspDto);
            if (loginRspDto == null && StringUtils.isEmpty(loginRspDto.getUid())) {
                if (loginReqDto.getLoginType() == 1 ) {
                    loginRspDto = facebookLoginService.loginCheck(loginReqDto, loginRspDto);
                } else if (loginReqDto.getLoginType() == 2) {
                    loginRspDto = googleLoginService.loginCheck(loginReqDto, loginRspDto);
                }
                // 第三方授权异常
                if (loginRspDto == null) {
                    logger.error("登陆授权异常，请稍后再试 empty {}", loginReqDto);
                    return null;
                }
            }
            return refreshUserTokenByLoginRspDto(loginReqDto, stopwatch, loginRspDto, isNeedCheck);
        } catch (Exception e) {
            logger.error("登陆授权异常，请稍后再试 {}", loginReqDto, e);
            return null;
        }
    }

    @Override
    public Boolean confirmRepeatedLogin(RepeatLoginReqDto repeatLoginReqDto) {
        return platTokenManagerService.refreshNewToken(repeatLoginReqDto);
    }

    private LoginRspDto refreshUserTokenByLoginRspDto(LoginReqDto loginReqDto, Stopwatch stopwatch,
                                                      LoginRspDto loginRspDto, Boolean isNeedCheck) throws Exception {
        // 生成新的token,刷新第三方access_token
        String token = null;
        if (loginReqDto.getIsRepeatedLogin()) {
            token = platTokenManagerService.makRepeatedPlatToken(loginRspDto.getUid(), loginReqDto.getOpenId(),
                                                                 Constant.EXPIRE_TIME_TWO_DAYS,
                                                                 loginRspDto.getLocation(), loginRspDto.getLoginType());
        } else {
            token = platTokenManagerService.makePlatToken(loginRspDto.getUid(), loginReqDto.getOpenId(), null,
                                                          loginRspDto.getLocation(), loginRspDto.getLoginType());
        }
        if (StringUtils.isEmpty(loginRspDto.getAid())) {
            String aid = platTokenManagerService.getSecretUid(loginRspDto.getUid());
            loginRspDto.setAid(aid);
        }

        // 访问了第三方(facebook)才能刷新token
        if (isNeedCheck && loginReqDto.getLoginType() == 1) {
            ThreadPool.commonSubmit(() -> {
            try {
                        // 刷新facebook的accesstoken
                        LoginRspDto rspDto = facebookLoginService.loginCheck(loginReqDto,loginRspDto);
                        UpdateUserInfoReqDto updateUserInfoReqDto = new UpdateUserInfoReqDto();
                        updateUserInfoReqDto.setAccessToken(rspDto.getAccessToken());
                        updateUserInfoReqDto.setOpenId(rspDto.getOpenId());
                        updateUserInfoReqDto.setExpires(rspDto.getExpires());
                        updateUserInfoReqDto.setLoginType(rspDto.getLoginType());
                        userManagerService.refreshUserAccessToken(updateUserInfoReqDto);
                    } catch (Exception e) {
                logger.error("[userManagerService.refreshUserAccessToken] error,loginRspDto:{}", loginRspDto, e);
            }
            });
        }
        logger.warn(" login success,login end openid:{},uid:{},token:{},nickName:{},cost time:{}",
                    loginReqDto.getOpenId(), loginRspDto.getUid(), token, loginRspDto.getNickName(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS));
        loginRspDto.setToken(token);
        return loginRspDto;
    }

    private LoginRspDto registerUserByLoginRspDto(LoginReqDto loginReqDto, Stopwatch stopwatch,
                                                  LoginRspDto loginRspDto) throws Exception {
        // 新用户注册，生成新的token
        String uid = platTokenManagerService.getPlatUid();
        if (StringUtils.isEmpty(uid)) {
            logger.error("登陆授权异常，新用户获取uid失败，uid:{}", uid);
            return null;
        }
        String aid = platTokenManagerService.getSecretUid(uid);
        String token = platTokenManagerService.makePlatToken(uid, loginReqDto.getOpenId(), null,
                                                             loginRspDto.getLocation(), loginRspDto.getLoginType());
        UserRegisterReqDto userRegisterReqDto = constructUserRegisterReqDto(loginRspDto, uid, aid);
        loginRspDto.setUid(uid);
        loginRspDto.setAid(aid);
        loginRspDto.setOid(String.valueOf(Long.parseLong(uid) - HeraclesAIUsersConfig.SUB_OID));
        userManagerService.registerFormalUser(userRegisterReqDto);
        loginRspDto.setToken(token);
        logger.warn(" login success,login end cost time:{},openid:{},uid:{},token:{},nickName:{}",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), loginReqDto.getOpenId(), loginRspDto.getUid(), token,
                    loginRspDto.getNickName());
        return loginRspDto;
    }

    @Override
    public String getUidByToken(String token) {
        // TODO 自动生成的方法存根
        return platTokenManagerService.getUidByToken(token);
    }

}
