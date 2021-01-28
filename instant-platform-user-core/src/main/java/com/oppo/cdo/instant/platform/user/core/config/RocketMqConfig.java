package com.oppo.cdo.instant.platform.user.core.config;

import com.oppo.cdo.instant.platform.common.core.mq.MqMsgSender;
import com.oppo.cdo.instant.platform.common.rocketmq.Producer;
import com.oppo.cdo.instant.platform.common.rocketmq.PushConsumer;
import com.oppo.cdo.instant.platform.user.core.mq.ConsumerAuthMsgListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 *
 * @author ouyangrenyong
 * @since 2.0
 */
@Configuration
public class RocketMqConfig {

    @Autowired
    private HeraclesConfig heraclesConfig;


    @Bean(initMethod = "init",destroyMethod = "destroy")
    public Producer webSocketProducer(){
        Producer producer = new Producer();
        producer.setInstanceName("webSocket");
        producer.setGroup("instant-platform-user-rpc-producer");
        producer.setCompressMsgBodyOverHowmuch(4096);
        producer.setNamesrvAddr(heraclesConfig.getMqProperties().getProperty("rocketmq.websocket.servers"));
        producer.setSendMsgTimeout((Integer) heraclesConfig.getMqProperties().getOrDefault("rocketmq.sendMsgTimeout", 200));
        return producer;
    }

    @Bean(initMethod = "init",destroyMethod = "destroy")
    public Producer platformProducer(){
        Producer producer = new Producer();
        producer.setInstanceName("platform");
        producer.setGroup("instant-platform-user-rpc-producer");
        producer.setCompressMsgBodyOverHowmuch(4096);
        producer.setNamesrvAddr(heraclesConfig.getMqProperties().getProperty("rocketmq.servers"));
        producer.setSendMsgTimeout((Integer) heraclesConfig.getMqProperties().getOrDefault("rocketmq.sendMsgTimeout", 200));
        return producer;
    }

    @Bean
    public MqMsgSender webSocketMqMsgSender(){
        return new MqMsgSender(webSocketProducer());
    }

    @Bean
    public MqMsgSender platformMqMsgSender(){
        return new MqMsgSender(platformProducer());
    }


    @Bean(initMethod = "init",destroyMethod = "destroy")
    public PushConsumer authConsumer(@Autowired ConsumerAuthMsgListener consumerAuthMsgListener){
        PushConsumer pushConsumer = new PushConsumer();
        pushConsumer.setInstanceName("webSocket");
        pushConsumer.setNamesrvAddr(heraclesConfig.getMqProperties().getProperty("rocketmq.websocket.servers"));
        pushConsumer.setGroup("instant-platform-user-auth-consumer");
        pushConsumer.setSubscribes(heraclesConfig.getMqProperties().getProperty("user.rocketmq.subscribes.authMsg"));
        pushConsumer.setMessageModel("CLUSTERING");
        pushConsumer.setConsumeThreadMin(Integer.parseInt(heraclesConfig.getMqProperties().getProperty("rocketmq.consumerThreadMin","30")));
        pushConsumer.setConsumeThreadMax(Integer.parseInt(heraclesConfig.getMqProperties().getProperty("rocketmq.consumerThreadMax","30")));
        pushConsumer.setConsumeMessageBatchMaxSize(Integer.parseInt(heraclesConfig.getMqProperties().getProperty("rocketmq.consumerMessageBatchMaxSize","1")));
        pushConsumer.setPullThresholdForQueue(Integer.parseInt(heraclesConfig.getMqProperties().getProperty("rocketmq.pullThresholdForQueue","1000")));
        pushConsumer.setConsumeConcurrentlyMaxSpan(Integer.parseInt(heraclesConfig.getMqProperties().getProperty("rocketmq.consumeConcurrentlyMaxSpan","2000")));
        pushConsumer.setPullBatchSize(Integer.parseInt(heraclesConfig.getMqProperties().getProperty("rocketmq.pullBatchSize","32")));
        pushConsumer.setMessageListener(consumerAuthMsgListener);
        return pushConsumer;
    }

}
