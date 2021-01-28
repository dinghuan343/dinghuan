//package com.oppo.cdo.instant.platform.user.core.util;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
//
//import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
//import org.springframework.stereotype.Component;
//
//import com.oppo.trace.threadpool.TraceThreadPoolExecutor;
//
///**
// * <p>Description: TODO </p>
// * <p>Copyright (c) 2019 www.oppo.com Inc. All rights reserved.</p>
// * <p>Company: OPPO</p>
// *
// * @Author: 80261587
// * @Since: 2020/1/16
// */
//
//@Component
//public class AsyncExecutor {
//    private int cpuCores = Runtime.getRuntime().availableProcessors();
//    private int maxPoolSize = cpuCores >= 20 ? cpuCores : cpuCores * 2;
//    private int corePoolSize = cpuCores / 2 + 1;
//    private final ExecutorService ASYNC_CACEHUSER_EXECUTOR = new TraceThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(50000), new CustomizableThreadFactory("cacheUserInfo"));
//    private final ExecutorService ASYNC_REFRESHACCESSTOKEN_EXECUTOR = new TraceThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(50000), new CustomizableThreadFactory("refreshAccessToken"));
//
//    public ExecutorService getRedisUserExecutor() {
//        return ASYNC_CACEHUSER_EXECUTOR;
//    }
//
//    public ExecutorService getRefreshAccessTokenExecutor() {
//        return ASYNC_REFRESHACCESSTOKEN_EXECUTOR;
//    }
//
//}
