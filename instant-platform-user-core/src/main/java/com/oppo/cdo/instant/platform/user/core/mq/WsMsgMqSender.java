package com.oppo.cdo.instant.platform.user.core.mq;

import com.oppo.cdo.instant.platform.common.base.ws.WsMessageGenerater;
import com.oppo.cdo.instant.platform.common.core.mq.MqMsgSender;
import com.oppo.cdo.instant.platform.common.domain.ws.WsMessage;
import com.oppo.cdo.instant.platform.common.domain.ws.WsMsgHeader;
import com.oppo.cdo.instant.platform.user.core.service.UserInfoManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2018/12/17 15:44
 */
@Component
public class WsMsgMqSender {
    private static final Logger logger = LoggerFactory.getLogger(WsMsgMqSender.class);
    private static final String ROBOT_SESSION = "0";
    private ThreadPoolExecutor poolExecutor;
    
    public WsMsgMqSender() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.min(cpuCores, 8);
        int maxPoolSize = cpuCores >= 8 ? cpuCores : cpuCores * 2;
        this.poolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));
    }
    
    @Autowired
    private UserInfoManager userInfoManager;
    
    @Autowired
    @Qualifier("webSocketMqMsgSender")
    private MqMsgSender mqMsgSender;
    
    public boolean sendWsMsgBySessionInfo(int msgId, Object msg, String sessionInfo, String uid) {
        try {
            if (ROBOT_SESSION.equals(sessionInfo)) {
                logger.warn("do not need send message to MQ, get sessionInfo by uid({}) is {}, this user is robot! msgId:{}, msg:{}",
                    uid, sessionInfo, msgId, msg);
                return true;
            }
            
            if (StringUtils.isBlank(sessionInfo)) {
                logger.error("send message to MQ failed, get sessionInfo by uid({}) failed! msgId:{}, msg:{}", uid, msgId, msg);
                return false;
            }
            
            WsMessage message = WsMessageGenerater.generate(uid, msgId, msg, sessionInfo);
            boolean sendResult = mqMsgSender.sendWsMsg(message);
            if (sendResult) {
                logger.debug("send message to MQ success, uid:{}, msgId:{}, msg:{}", uid, msgId, msg);
            } else {
                logger.error("send message to MQ failed, uid:{}, msgId:{}, msg:{}", uid, msgId, msg);
            }
            
            return sendResult;
        } catch (Exception e) {
            logger.error("send message to MQ failed, uid:{}, msgId:{}, msg:{}, Exception \n", uid, msgId, msg, e);
            return false;
        }
    }
    
    public boolean asyncSendWsMsgByMsgHeader(int msgId, Object msg, WsMsgHeader wsMsgHeader) {
        try {
            poolExecutor.submit(() -> sendWsMsgByMsgHeader(msgId, msg, null, wsMsgHeader));
            return true;
        } catch (Exception e) {
            logger.error("async send message to MQ failed, msgId:{}, msg:{}, Exception \n", msgId, msg, e);
            return false;
        }
    }
    
    public boolean sendWsMsgByMsgHeader(int msgId, Object msg, WsMsgHeader wsMsgHeader) {
        return sendWsMsgByMsgHeader(msgId, msg, null, wsMsgHeader);
    }
    
    public boolean sendWsMsgByMsgHeader(int msgId, Object msg, Map<String, Object> extraInfo, WsMsgHeader wsMsgHeader) {
        try {
            WsMessage message = WsMessageGenerater.generate(msgId, msg, extraInfo, wsMsgHeader);

            boolean sendResult = mqMsgSender.sendWsMsg(message);
            
            String uid = message.getWsMsgHeader().getUid();
            if (sendResult) {
                logger.debug("send message to MQ success, reqMsgId:{}, connId:{}, rspMsgId:{}, uid:{}, msg:{}",
                    wsMsgHeader.getMsgId(), wsMsgHeader.getConnId(), msgId, uid, msg);
            } else {
                logger.error("send message to MQ failed, reqMsgId:{}, connId:{}, rspMsgId:{}, uid:{}, msg:{}",
                    wsMsgHeader.getMsgId(), wsMsgHeader.getConnId(), msgId, uid, msg);
            }
            return sendResult;
        } catch (Exception e) {
            logger.error("send message to MQ failed, reqMsgId:{}, connId:{}, rspMsgId:{}, msg:{}",
                wsMsgHeader.getMsgId(), wsMsgHeader.getConnId(), msgId, msg, e);
            return false;
        }
    }
}
