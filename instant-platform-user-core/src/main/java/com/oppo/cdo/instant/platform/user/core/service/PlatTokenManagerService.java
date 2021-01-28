package com.oppo.cdo.instant.platform.user.core.service;

import com.oppo.cdo.instant.platform.common.base.util.AESUtil;
import com.oppo.cdo.instant.platform.common.domain.base.Result;
import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.idgenerator.facade.CommonUidService;
import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;
import com.oppo.cdo.instant.platform.user.domain.common.Constant;
import com.oppo.cdo.instant.platform.user.domain.common.RedisKey;
import com.oppo.cdo.instant.platform.user.domain.dto.req.RepeatLoginReqDto;
import com.oppo.cdo.instant.platform.user.domain.type.LoginType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2019/10/30 17:41
 */
@Component
public class PlatTokenManagerService {

    private static final Logger logger                 = LoggerFactory.getLogger(PlatTokenManagerService.class);
    private static final String TOKEN_STRING_CONNECTOR = "_";
    private static final int    TOKEN_STRING_ARRAY_LEN = 2;
    private static final int    TOKEN_INFO_ARRAY_LEN   = 3;
    private static final String REPEATED_PREX          = "RE_";
    private static final String GAME_INSTANT_TAG       = "game_instant_tag";
    private static String       tokenSecretKey;
    private static Boolean      isAllowTestUser        = false;
    private static String       testFacebookTokenValue;
    private static String       testGoogleTokenValue;
    private static String       testUserTokens;

    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;
    @Autowired
    private CommonUidService    commonUidService;
    @Autowired
    private HeraclesConfig      heraclesConfig;

    /**
     * 初始化拨测数据
     */
    @PostConstruct
    public void initApolloTestConfig() throws Exception {
        String secretKeyInfo = heraclesConfig.getTokenSecretKey();
        isAllowTestUser = heraclesConfig.getIsAllowTestUser();
        testFacebookTokenValue = heraclesConfig.getTestFaceBookToken();
        testGoogleTokenValue = heraclesConfig.getTestGoogleToken();
        testUserTokens = heraclesConfig.getTestTrueTokens();
        if (StringUtils.isEmpty(secretKeyInfo)) {
            logger.warn("get token secretKey failed!");
            throw new Exception("get token secretKey failed!");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("token secretKey is {}", secretKeyInfo);
        }
        tokenSecretKey = new String(Base64.decodeBase64(secretKeyInfo));
        if (StringUtils.isEmpty(tokenSecretKey)) {
            logger.warn("get token secretKey failed!");
            throw new Exception("get token secretKey failed!");
        }
    }

    public String getPlatUid() throws Exception {
        Result<Long> result = commonUidService.getIncrDistributedId(GAME_INSTANT_TAG);
        if (!result.isSuccess() || result.getData() == null) {
            logger.warn("getTouristUserInfo end, get platform uid failed! reason:{}", result.getMessage());
            throw new Exception("get platform uid failed");
        }
        return String.valueOf(result.getData());
    }

    /**
     * 自动登陆生成token，不清除旧token，新token暂时缓存俩天
     * 
     * @param uid
     * @param openId
     * @param expires
     * @param location
     * @param loginType
     * @return
     * @throws Exception
     */
    public String makRepeatedPlatToken(String uid, String openId, String expires, String location,
                                       Integer loginType) throws Exception {
        if (StringUtils.isEmpty(location) || StringUtils.isEmpty(uid) || StringUtils.isEmpty(openId)) {
            logger.error("[makePlatToken]  params empty error,uid:{},openId:{],location:{}", uid, openId, location);
            throw new RuntimeException();
        }
        String token = location + (LoginType.getLoginTypePrex(loginType)) + REPEATED_PREX + getToken(uid, openId);
        int expiresTime = Integer.parseInt(expires);
        // 存 token 与 uid 的关系
        String redisKey = RedisKey.getUserTokenKey(uid);
        redisClusterManager.hset(redisKey, token, String.valueOf(new Date().getTime() + (long) expiresTime * 1000));
        redisClusterManager.expire(redisKey, expiresTime);
        logger.debug("makePlatToken, uid:{}, openId:{}, new token:{}", uid, token);
        return token;
    }

    public String makePlatToken(String uid, String openId, String expires, String location,
                                Integer type) throws Exception {
        if (StringUtils.isEmpty(location) || StringUtils.isEmpty(uid) || StringUtils.isEmpty(openId)) {
            logger.error("[makePlatToken]  params empty error,uid:{},openId:{],location:{}", uid, openId, location);
            throw new RuntimeException();
        }
        String token = location + (LoginType.getLoginTypePrex(type)) + TOKEN_STRING_CONNECTOR + getToken(uid, openId);
        int expiresTime = heraclesConfig.getUserTokenExpiresTime();
        String redisKey = RedisKey.getUserTokenKey(uid);
        redisClusterManager.del(redisKey);
        // 存 uid 与 token 的关系
        redisClusterManager.hset(redisKey, token, String.valueOf(new Date().getTime() + (long) expiresTime * 1000));
        redisClusterManager.expire(redisKey, expiresTime);
        if (logger.isDebugEnabled()) {
            logger.debug("makePlatToken, redisKey:{}, openId:{}, new token:{}", redisKey, openId, token);
        }
        return token;
    }

    public Boolean refreshNewToken(RepeatLoginReqDto repeatLoginReqDto) {
        String preToken = repeatLoginReqDto.getPrePlatToken();
        String newToken = repeatLoginReqDto.getNewPlatToken();
        String uid = null;
        if (StringUtils.isEmpty(preToken) || StringUtils.isEmpty(newToken)) {
            logger.error("refreshNewToken error,preToken or newToken is empty,preToken:{},newToken:{}", preToken,
                         newToken);
            return false;
        }
        uid = getUidByToken(preToken);
        redisClusterManager.hset(RedisKey.getUserTokenKey(uid), preToken,
                                 String.valueOf(new Date().getTime()
                                                + Long.parseLong(Constant.EXPIRE_TIME_ONE_HOUR) * 1000));
        int expiresTime = heraclesConfig.getUserTokenExpiresTime();
        redisClusterManager.hset(RedisKey.getUserTokenKey(uid), newToken,
                                 String.valueOf(new Date().getTime() + (long) expiresTime * 1000));

        if (logger.isDebugEnabled()) {
            logger.debug("refreshNewToken success, uid:{}, openId:{}, new token:{}", uid, newToken);
        }
        return true;
    };

    public Boolean checkToken(String token) throws Exception {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        String uid = getUidByToken(token);
        String expireTime = redisClusterManager.hget(RedisKey.getUserTokenKey(uid), token);
        if (StringUtils.isNotBlank(expireTime) && (new Date().getTime() <= Long.parseLong(expireTime))) {
            return true;
        } else {
            redisClusterManager.hdel(RedisKey.getUserTokenKey(uid), token);
            if (logger.isDebugEnabled()) {
                logger.debug("token expire, uid:{} , token:{} , expireTime:{}", uid, token, expireTime);
            }
        }
        // 测试token直接通过
        if (isAllowTestUser && (token.equals(testFacebookTokenValue) || token.equals(testGoogleTokenValue)
                                || testUserTokens.contains(token))) {
            return true;
        }
        return false;
    }

    public String getUidFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.warn("getUidFromToken failed! token is empty!");
            return StringUtils.EMPTY;
        }

        try {
            String[] strArray = token.split(TOKEN_STRING_CONNECTOR);
            if (strArray == null || strArray.length < TOKEN_STRING_ARRAY_LEN) {
                logger.warn("getUidFromToken failed! token:{}", token);
                return StringUtils.EMPTY;
            }

            String[] array = parseToken(strArray[1]);
            if (array == null || array.length < TOKEN_INFO_ARRAY_LEN) {
                logger.warn("getUidFromToken failed! token:{}", token);
                return StringUtils.EMPTY;
            }

            String uid = array[0];
            if (logger.isDebugEnabled()) {
                logger.debug("getUidFromToken from token:{} is {}", token, uid);
            }
            return uid;
        } catch (Exception e) {
            logger.warn("getUidFromToken failed! token:{}, exception ", token, e);
            return StringUtils.EMPTY;
        }
    }

    public String getUidByAid(String aid) {
        if (StringUtils.isEmpty(aid)) {
            logger.warn("getUidFromOid failed! oid is empty!");
            return StringUtils.EMPTY;
        }

        try {
            String str = AESUtil.decrypt(new String(Base64.decodeBase64(aid)), tokenSecretKey);
            String[] strs = str.split("_");
            if (strs.length == 1) {
                return str;
            } else {
                String currentTime = strs[0];
                if (System.currentTimeMillis() - Long.parseLong(currentTime) < heraclesConfig.getAidExpiresSecondsTime()
                                                                               * 1000) {
                    return strs[1];
                }
                return StringUtils.EMPTY;
            }
        } catch (Exception e) {
            logger.warn("getUidFromOid failed! oid:{}, exception ", aid, e);
            return StringUtils.EMPTY;
        }
    }

    public long getTimestampFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.warn("getTimestampFromToken failed! token is empty!", token);
            return 0L;
        }

        try {
            String[] strArray = token.split(TOKEN_STRING_CONNECTOR);
            if (strArray == null || strArray.length < TOKEN_STRING_ARRAY_LEN) {
                logger.warn("getUidFromToken failed! token:{}", token);
                return 0L;
            }

            String[] array = parseToken(strArray[1]);
            if (array == null || array.length < TOKEN_INFO_ARRAY_LEN) {
                logger.warn("getTimestampFromToken failed! token:{}", token);
                return 0L;
            }

            long tokenCreateTimeStamp = Long.parseLong(array[2]);
            logger.debug("getTimestampFromToken from token:{} is {}", token, tokenCreateTimeStamp);
            return tokenCreateTimeStamp;
        } catch (Exception e) {
            logger.warn("getTimestampFromToken failed! token:{}, exception ", token, e);
            return 0L;
        }
    }

    /**
     * 根据uid、openId 和 当前时间戳生成大厅token
     * 
     * @param uid
     * @param openId
     * @return
     * @throws Exception
     */
    public static String getToken(String uid, String openId) throws Exception {
        long timeStamp = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(uid).append(TOKEN_STRING_CONNECTOR).append(openId).append(TOKEN_STRING_CONNECTOR).append(timeStamp);

        String token = AESUtil.encrypt(sb.toString(), tokenSecretKey);
        token = Base64.encodeBase64URLSafeString(token.getBytes());
        return token;
    }

    /**
     * 加密uid
     * 
     * @return
     * @throws Exception
     */
    public static String getSecretUid(String uid) {
        if (StringUtils.isEmpty(uid)) {
            logger.warn("getSecretUid end, get oid failed! uid:{}", uid);
            throw new RuntimeException("get platform oid failed");
        }
        String oid = null;
        try {
            oid = AESUtil.encrypt(uid, tokenSecretKey);
            oid = Base64.encodeBase64URLSafeString(oid.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return oid;
    }


    public String getUidByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.warn("uid get by token failed! token is empty!");
            return StringUtils.EMPTY;
        }

        String uid = parseUidFromToken(token);
        if (StringUtils.isNotEmpty(uid)) {
            if (logger.isDebugEnabled()) {
                logger.debug("uid get by token:{} is {}", token, uid);
            }
            return uid;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("uid get by token:{} is empty!", token);
        }
        return StringUtils.EMPTY;
    }

    private String parseUidFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.warn("parseUidFromToken failed! token is empty!", token);
            return StringUtils.EMPTY;
        }

        try {
            String[] strArray = token.split(TOKEN_STRING_CONNECTOR);
            if (strArray == null || strArray.length < TOKEN_STRING_ARRAY_LEN) {
                logger.warn("parseUidFromToken failed! token:{}", token);
                return StringUtils.EMPTY;
            }

            String[] array = parseToken(strArray[1]);
            if (array == null || array.length < TOKEN_INFO_ARRAY_LEN) {
                logger.warn("parseUidFromToken failed! token:{}", token);
                return StringUtils.EMPTY;
            }

            String uid = array[0];
            if (logger.isDebugEnabled()) {
                logger.debug("parseUidFromToken from token:{} is {}", token, uid);
            }
            return uid;
        } catch (Exception e) {
            logger.warn("parseUidFromToken failed! token:{}, exception ", token, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 反解token信息
     * 
     * @param token
     * @return String[] uid : String[0] 大厅用户id channelUid : String[1] 用户OPENID timeStamp : String[2] 时间戳
     */
    private String[] parseToken(String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            throw new Exception("token or secretKeyString is null!");
        }

        String parseToken = new String(Base64.decodeBase64(token));
        if (StringUtils.isEmpty(parseToken)) {
            throw new Exception("parse token: " + token + " failed!");
        }

        String tokenInfo = AESUtil.decrypt(parseToken, tokenSecretKey);
        if (StringUtils.isEmpty(tokenInfo)) {
            throw new Exception("parse token: " + token + " failed!");
        }

        String[] array = tokenInfo.split(TOKEN_STRING_CONNECTOR);
        return array;
    }

    public static void main(String[] args) throws Exception {
        tokenSecretKey = new String(Base64.decodeBase64("c2ZxSkhubzRwcm93Z1hrSmxvOWhVQT09"));
        String oid = null;
        oid = AESUtil.encrypt("445311488", tokenSecretKey);
        oid = Base64.encodeBase64URLSafeString(oid.getBytes());
        System.out.println(oid);
    }

}
