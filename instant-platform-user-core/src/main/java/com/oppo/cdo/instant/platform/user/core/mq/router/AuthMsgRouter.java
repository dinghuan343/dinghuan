package com.oppo.cdo.instant.platform.user.core.mq.router;

import com.oppo.cdo.instant.platform.common.core.mq.MqMsgProcessor;
import com.oppo.cdo.instant.platform.common.domain.ws.WsMessage;
import com.oppo.cdo.instant.platform.user.core.mq.handler.LoginHandler;
import com.oppo.intl.instant.game.proto.websocket.MsgIdDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2018/12/10 15:29
 */

@Component
public class AuthMsgRouter extends MqMsgProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthMsgRouter.class);
    
    @Autowired
    private LoginHandler loginHandler;
    
    @Override
    protected void processMsg(WsMessage wsMessage) throws Exception {
        if (wsMessage == null || wsMessage.getWsMsgHeader() == null) {
            logger.error("processMsg failed, WsMessage is inValid! {}", wsMessage);
            return;
        }
        
        int msgId = wsMessage.getWsMsgHeader().getMsgId();
        if (logger.isDebugEnabled()) {
            logger.debug("start processMsg:{}", msgId);
        }
        
        switch (msgId) {
            case MsgIdDef.Msg_C2S_LoginPlatReqID:
                loginHandler.login(wsMessage);
                break;
            case MsgIdDef.Msg_C2S_ReConnectReqID:
                loginHandler.reconnect(wsMessage);
                break;
            case MsgIdDef.Msg_C2S_LogOutPlatform:
                loginHandler.logout(wsMessage);
                break;
            default:
                logger.warn("can't process this message, msgId:{}", msgId);
                break;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("end processMsg:{}", msgId);
        }
    }
}
