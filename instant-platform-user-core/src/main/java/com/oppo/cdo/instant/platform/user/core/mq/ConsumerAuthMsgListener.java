package com.oppo.cdo.instant.platform.user.core.mq;

import com.oppo.cdo.instant.platform.common.core.mq.AbstractMsgConsumer;
import com.oppo.cdo.instant.platform.user.core.mq.router.AuthMsgRouter;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2018/12/10 11:19
 */
@Component
@DependsOn("springBeanUtil")
public class ConsumerAuthMsgListener extends AbstractMsgConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerAuthMsgListener.class);
    
    @Autowired
    private AuthMsgRouter msgProcessor;
    
    @Override
    @PostConstruct
    public void init() throws Exception {
        super.init();
    }

    @Override
    protected void processMsg(MessageExt msg) throws Exception {
        msgProcessor.process(msg.getBody());
    }
}
