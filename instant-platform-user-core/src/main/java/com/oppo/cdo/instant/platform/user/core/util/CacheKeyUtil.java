package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.user.domain.common.RedisKey;

import java.util.Arrays;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2018/7/5 10:30
 */
public class CacheKeyUtil {
    
    public static String getUserBasicCacheKey(String... parameters) {
        if (parameters == null || parameters.length == 0) {
            return RedisKey.getUserInfoUidKey();
        }

        return getCacheKey(RedisKey.getUserInfoUidKey(), parameters);
    }

    public static  String getUserBasicCacheOpenIdKey(String... parameters){
        if (parameters == null || parameters.length == 0) {
            return RedisKey.getUserInfoOpenidKey();
        }
        return getCacheKey(RedisKey.getUserInfoOpenidKey(), parameters);
    }
    
    private static String getCacheKey(String prefix, String... parameters) {
        StringBuilder sb = new StringBuilder(prefix);
        Arrays.stream(parameters).forEach((parameter) -> sb.append(parameter).append("_"));
        sb.delete(sb.lastIndexOf("_"), sb.length());
        return sb.toString();
    }
}
