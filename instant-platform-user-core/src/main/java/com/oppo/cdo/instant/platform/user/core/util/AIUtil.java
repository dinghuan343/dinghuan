package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.user.core.config.HeraclesConfig;

public class AIUtil {

    public static boolean isAI(String uid) {
        
        HeraclesConfig heraclesConfig = HeraclesConfig.getHeraclesConfig();
        if (Long.parseLong(uid) >= heraclesConfig.getCurrServerAIUidMin().longValue()
            && Long.parseLong(uid) <= heraclesConfig.getCurrServerAIUidMax().longValue()) {
            return true;
        }
        return false;
    }
}
