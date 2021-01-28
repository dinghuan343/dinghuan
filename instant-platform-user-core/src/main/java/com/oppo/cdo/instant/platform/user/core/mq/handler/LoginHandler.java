package com.oppo.cdo.instant.platform.user.core.mq.handler;

import com.alibaba.fastjson.JSON;
import com.google.api.client.json.Json;
import com.google.common.base.Preconditions;
import com.oppo.cdo.instant.platform.common.base.util.ClientHeaderUtil;
import com.oppo.cdo.instant.platform.common.base.util.ProtoBufUtil;
import com.oppo.cdo.instant.platform.common.base.util.SessionInfoUtil;
import com.oppo.cdo.instant.platform.common.domain.base.ClientHeader;
import com.oppo.cdo.instant.platform.common.domain.type.CommonCode;
import com.oppo.cdo.instant.platform.common.domain.type.SeriaStrategy;
import com.oppo.cdo.instant.platform.common.domain.ws.WsMessage;
import com.oppo.cdo.instant.platform.common.domain.ws.WsMsgHeader;
import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.user.core.cache.SessionInfoMapBuilder;
import com.oppo.cdo.instant.platform.user.core.mq.WsMsgMqSender;
import com.oppo.cdo.instant.platform.user.core.service.PlatTokenManagerService;
import com.oppo.cdo.instant.platform.user.core.service.UserInfoManager;
import com.oppo.cdo.instant.platform.user.core.service.UserSessionInfoManager;
import com.oppo.cdo.instant.platform.user.domain.type.OfflineType;
import com.oppo.game.instant.platform.proto.MsgIdDef;
import com.oppo.game.instant.platform.proto.request.LoginPlatReq;
import com.oppo.game.instant.platform.proto.request.ReConnectReq;
import com.oppo.game.instant.platform.proto.response.ForceOfflineRsp;
import com.oppo.game.instant.platform.proto.response.LoginPlatRsp;
import com.oppo.game.instant.platform.proto.response.ReConnectPlatRsp;
import com.oppo.instant.game.web.proto.login.ResponseKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2018/12/10 20:10
 */
@Component
public class LoginHandler extends AbstractHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private ThreadPoolExecutor rtStaticsPoolExecutor;
    
    @Autowired
    private PlatTokenManagerService platTokenManagerService;
    
    @Autowired
    private WsMsgMqSender wsMsgMqSender;
    
    @Autowired
    private UserInfoManager userInfoManager;

    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;

    @Autowired
    private UserSessionInfoManager userSessionInfoManager;
    
    @PostConstruct
    public void init() {
        rtStaticsPoolExecutor = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000));
    }
    
    public void login(WsMessage wsMessage) throws Exception {
        WsMsgHeader wsMsgHeader = wsMessage.getWsMsgHeader();
        try {
            Preconditions.checkArgument((wsMessage.getWsMsgBody() != null), "wsMsgBody is null");
            
            LoginPlatReq loginReq = ProtoBufUtil.decode(LoginPlatReq.class, wsMessage.getWsMsgBody().getBody(), wsMsgHeader.getMsgType(),
                SeriaStrategy.getById(wsMsgHeader.getSeriaStrategy()));
            Preconditions.checkArgument((loginReq != null), "LoginPlatReq is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(loginReq.getUid()), "uid is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(loginReq.getToken()), "token is null");
            
            String uid = loginReq.getUid();
            logger.debug("user:{} login platform websocket, {}", uid, loginReq);
            String location = null;
            if (null != loginReq.getLatitude() && null != loginReq.getLongitude()){
                location = loginReq.getLongitude()+","+loginReq.getLatitude();
            }
            loginAndGetUserInfo(wsMsgHeader, uid, loginReq.getToken(), loginReq.getId(), location, false);
        } catch (IllegalArgumentException e) {
            logger.warn("login failed! msgId:{}, reason:{}", wsMsgHeader.getMsgId(), e.getMessage());
        }
    }
    
    public void reconnect(WsMessage wsMessage) throws Exception {
        WsMsgHeader wsMsgHeader = wsMessage.getWsMsgHeader();
        try {
            Preconditions.checkArgument((wsMessage.getWsMsgBody() != null), "wsMsgBody is null");
            
            ReConnectReq reConnectReq = ProtoBufUtil.decode(ReConnectReq.class, wsMessage.getWsMsgBody().getBody(), wsMsgHeader.getMsgType(),
                SeriaStrategy.getById(wsMsgHeader.getSeriaStrategy()));
            Preconditions.checkArgument((reConnectReq != null), "reConnectReq is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(reConnectReq.getUid()), "uid is null");
            Preconditions.checkArgument(StringUtils.isNotEmpty(reConnectReq.getToken()), "token is null");
            
            String uid = reConnectReq.getUid();
            logger.debug("user:{} reConnect platform websocket, {}", uid, reConnectReq);

            loginAndGetUserInfo(wsMsgHeader, uid, reConnectReq.getToken(), reConnectReq.getId(), null, true);
        } catch (IllegalArgumentException e) {
            logger.warn("reconnect failed! msgId:{}, reason:{}", wsMsgHeader.getMsgId(), e.getMessage());
        }
    }

    /**
     * websocket断开连接后，socket服务会推送这个事件
     * @param wsMessage
     * @throws Exception
     */
    public void logout(WsMessage wsMessage) throws Exception {
        WsMsgHeader wsMsgHeader = wsMessage.getWsMsgHeader();
        Map<String, Object> properties = wsMessage.getWsMsgHeader().getProperties();
        String sessionInfo = SessionInfoUtil.getSessionInfo(wsMsgHeader);

        try {

            String uid = (String) properties.get("uid");

            logger.debug("platLogout start, uid get by sessionId:{} is {}", sessionInfo, uid);
            if (StringUtils.isEmpty(uid)) {
                logger.warn("platLogout failed! uid get by sessionId:{} is null", sessionInfo);
                return;
            }
            String cacheSessionId = userSessionInfoManager.getSession(uid);

            if (StringUtils.isNotEmpty(cacheSessionId) && !cacheSessionId.equals(sessionInfo)) {
                //该用户可能已经重新登录了，为了防止并发导致错误删除数据
                logger.warn("platLogout success! cacheSessionId get by uid:{} is {}, it is not equals input sessionId:{}",
                        uid, cacheSessionId, sessionInfo);
                return;
            }

            userSessionInfoManager.logout(uid);
            logger.debug("platLogout success! sessionId:{}, uid:{}", sessionInfo, uid);
        } catch (Exception e) {
            logger.warn("logout failed! session:{}, exception\n", sessionInfo, e);
        }
    }

    
    private boolean loginAndGetUserInfo(WsMsgHeader wsMsgHeader, String uid, String token, String openId,
                                        String location, boolean isReconnect) throws Exception {
        boolean authResultFlg = false;
        Boolean tokenValid = platTokenManagerService.checkToken(token);
        if (tokenValid) {
            authResultFlg = true;
            
            String session = SessionInfoUtil.getSessionInfo(wsMsgHeader);
            if (logger.isDebugEnabled()) {
                logger.debug("user login websocket tokenValid! session:{}, uid: {}", session, uid);
            }
    
            ClientHeader clientHeader = new ClientHeader();
            ClientHeaderUtil.parseOpenId(clientHeader, openId);
            String deviceId = StringUtils.isNotBlank(clientHeader.getImei()) ? clientHeader.getImei() : clientHeader.getOUID();
            
            multiClientJudgment(session, openId, uid, deviceId);

            SessionInfoMapBuilder builder = new SessionInfoMapBuilder();
            builder.putDeviceId(deviceId);
            builder.putOnlineStatus(CommonCode.STATUS_ON_LINE.getCode());
            builder.putSession(session);
            builder.putLocation(location);

            //如果不是重连的话，需要设置用户的游戏状态为free
            if (!isReconnect) {
                builder.putPlayStatus(CommonCode.PLAYER_STATUS_FREE.getCode());
            }
            userSessionInfoManager.cacheUserSessionInfo(uid,builder);
        }
        
        if (isReconnect) {
            sendReconnectRspMsg(wsMsgHeader, authResultFlg, uid, tokenValid);
        } else {
            sendLoginRspMsg(wsMsgHeader, authResultFlg, uid, tokenValid);
        }
        
        return true;
    }
    
    private void sendReconnectRspMsg(WsMsgHeader wsMsgHeader, boolean authResult, String uid, Boolean tokenValid) throws Exception {
        ReConnectPlatRsp reConnectPlatRsp = new ReConnectPlatRsp();
        reConnectPlatRsp.setResult(authResult);
        reConnectPlatRsp.setPlayerStatus(CommonCode.PLAYER_STATUS_FREE.getCode());
        reConnectPlatRsp.setErrCode(tokenValid?null:ResponseKey.FAIL_TOKEN_INVALID.code());
        reConnectPlatRsp.setErrMsg(tokenValid?null:ResponseKey.FAIL_TOKEN_INVALID.msg());
        
        HashMap<String, Object> extraInfo = new HashMap<>(2);
        extraInfo.put("authResult", authResult);
        extraInfo.put("uid", uid);
        
        wsMsgHeader.setUid(uid);
        wsMsgMqSender.sendWsMsgByMsgHeader(MsgIdDef.Msg_C2S_ReConnectRspID, reConnectPlatRsp, extraInfo, wsMsgHeader);
    }
    
    private void sendLoginRspMsg(WsMsgHeader wsMsgHeader, boolean authResult, String uid, Boolean tokenValid) throws Exception {
        LoginPlatRsp loginPlatRsp = new LoginPlatRsp();
        loginPlatRsp.setResult(authResult);
        loginPlatRsp.setErrCode(tokenValid?ResponseKey.SUCCESS.code():ResponseKey.FAIL_TOKEN_INVALID.code());
        loginPlatRsp.setErrMsg(tokenValid?null:ResponseKey.FAIL_TOKEN_INVALID.msg());
        
        HashMap<String, Object> extraInfo = new HashMap<>(2);
        extraInfo.put("authResult", authResult);
        extraInfo.put("uid", uid);
        
        wsMsgHeader.setUid(uid);
        wsMsgMqSender.sendWsMsgByMsgHeader(MsgIdDef.Msg_C2S_LoginPlatRspID, loginPlatRsp, extraInfo, wsMsgHeader);
        
//        if (authResult) {
//            // push client config
//            String clientCfgJson = ApolloConfig.getClientCfgJson();
//            String clientLogCfgJson = ApolloConfig.getClientLogCfgJson();
//            ClientApolloCfg clientApolloCfg = new ClientApolloCfg(clientCfgJson, clientLogCfgJson);
//            wsMsgMqSender.sendWsMsgByMsgHeader(MsgIdDef.Msg_COM_ClientCfg, clientApolloCfg, wsMsgHeader);
//        }
    }
    
    private void multiClientJudgment(String sessionId, String openId, String uid, String deviceId) {
        String oldSession = userSessionInfoManager.getSession(uid);
        if (StringUtils.isEmpty(oldSession) || oldSession.equals(sessionId)) {
            return;
        }
        
        String oldDevId = userSessionInfoManager.getDeviceId(uid);
        if (StringUtils.isNotEmpty(deviceId) && deviceId.equals(oldDevId)) {
            logger.warn("player login in other client! oldSession:{} is not equals sessionId:{}, uid:{}, deviceId:{}, openId:{}",
                oldSession, sessionId, uid, deviceId, openId);
            quietOffline(oldSession, uid);
            return;
        }
        logger.warn("player login in other device! oldSession:{}, sessionId:{}, oldDevId:{}, devId:{}, openId:{}, uid:{}", oldSession,
            sessionId, oldDevId, deviceId, openId, uid);

        forceOffline(oldSession, uid);

    }
    
    private void quietOffline(String oldSession, String uid) {
        logger.warn("quietOffline user:{} other session:{}", uid, oldSession);
        ForceOfflineRsp forceOfflineRsp = new ForceOfflineRsp();
        forceOfflineRsp.setErrCode(OfflineType.QUIET.getCode());
        forceOfflineRsp.setErrMsg(OfflineType.QUIET.getMsg());
        wsMsgMqSender.sendWsMsgBySessionInfo(MsgIdDef.Msg_C2S_ForceOfflineID, forceOfflineRsp, oldSession, uid);
    }
    
    private void forceOffline(String oldSession, String uid) {
        logger.warn("forceOffline user:{} other session:{}", uid, oldSession);
        ForceOfflineRsp forceOfflineRsp = new ForceOfflineRsp();
        forceOfflineRsp.setErrCode(OfflineType.FORCE.getCode());
        forceOfflineRsp.setErrMsg(OfflineType.FORCE.getMsg());
        wsMsgMqSender.sendWsMsgBySessionInfo(MsgIdDef.Msg_C2S_ForceOfflineID, forceOfflineRsp, oldSession, uid);
    }
    

}
