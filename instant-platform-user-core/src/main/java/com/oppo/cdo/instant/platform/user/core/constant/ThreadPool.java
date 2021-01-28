package com.oppo.cdo.instant.platform.user.core.constant;

import esa.commons.concurrent.ThreadFactories;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description:
 * 线程池统一管理，线程池分为特定业务池和其他线程池，为了避免一些分支流程影响业务，所以像通知这些可以丢到其他线程池中处理
 * @author ouyangrenyong
 * @since 2.0
 */
public class ThreadPool {

    private final static int processors = Runtime.getRuntime().availableProcessors();

    /**
     * 业务线程池
     */
    private final static ThreadPoolExecutor businessPool = new ThreadPoolExecutor(
            processors,
            processors * 4,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(8000),
            ThreadFactories.namedThreadFactory("businessThreadPool"));

    /**
     * 通用线程池
     */
    private final static ThreadPoolExecutor commonPool = new ThreadPoolExecutor(
            processors,
            processors*10,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            ThreadFactories.namedThreadFactory("commonThreadPool"));


    public static void businessSubmit(Runnable runnable) {
        businessPool.submit(runnable);
    }

    /**
     * 不能用业务线程池的
     */
    public static void commonSubmit(Runnable runnable) {
        commonPool.submit(runnable);
    }

}

