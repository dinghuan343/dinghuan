package com.oppo.cdo.instant.platform.user.core.util;

import org.apache.commons.lang3.time.StopWatch;

/**
 * @author chenchangjiang 80229032
 * @Date: 2020/3/16 11:15
 */
public class RtUtil {
    private static ThreadLocal<StopWatch> stopWatchThreadLocal = new ThreadLocal<StopWatch>() {
        @Override
        protected StopWatch initialValue() {
            StopWatch stopWatch = new StopWatch();
            stopWatch.reset();
            stopWatch.start();
            return stopWatch;
        }
    };

    public static long getRt() {
        StopWatch stopWatch = stopWatchThreadLocal.get();
        long time = stopWatch.getTime();
        stopWatch.reset();
        stopWatch.start();
        return time;
    }

//    public static void main(String[] args) throws Exception {
//        System.out.println(RtUtil.getRt());
//
//        TimeUnit.SECONDS.sleep(1);
//        System.out.println(RtUtil.getRt());
//
//        TimeUnit.SECONDS.sleep(1);
//        System.out.println(RtUtil.getRt());
//    }
}
