package com.oppo.cdo.instant.platform.user.core.util;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2019/5/29 10:20
 */
public class RandomNumUtil {
    
    private RandomNumUtil() {
    
    }
    
    public static long getRandomNum() {
        return System.currentTimeMillis();
    }
}
