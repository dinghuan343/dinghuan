package com.oppo.cdo.instant.platform.user.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oppo.cdo.instant.platform.common.sensitive.SensitiveTextInfo;

@Component
/**
 * 敏感词和头衔检测配置
 */
public class SensitiveConfig {

    @Autowired
    private HeraclesConfig heraclesConfig;

    public SensitiveTextInfo getTextSensitive() {
        SensitiveTextInfo sensitiveTextInfo = new SensitiveTextInfo();
        sensitiveTextInfo.setSensitiveUrl(heraclesConfig.getSensitiveSynUrl());
        sensitiveTextInfo.setProductId(heraclesConfig.getSensitiveSynProductId());
        sensitiveTextInfo.setApisecret(heraclesConfig.getSensitiveSynApisecret());
        sensitiveTextInfo.setApikey(heraclesConfig.getSensitiveSynApikey());
        return sensitiveTextInfo;
    }

    public SensitiveTextInfo getImageSensitive() {
        SensitiveTextInfo sensitiveImage = new SensitiveTextInfo();
        sensitiveImage.setSensitiveUrl(heraclesConfig.getSensitiveImageUrl());
        sensitiveImage.setProductId(heraclesConfig.getSensitiveImageProductId());
        sensitiveImage.setApisecret(heraclesConfig.getSensitiveImageApisecret());
        sensitiveImage.setApikey(heraclesConfig.getSensitiveImageApikey());
        return sensitiveImage;
    }

}
