package com.coride.matching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadPoolConfig {

    @Value("${threadpool.corePoolSize}")
    private int corePoolSize;

    @Value("${threadpool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${threadpool.queueCapacity}")
    private int queueCapacity;

    @Value("${threadpool.keepAliveTime}")
    private long keepAliveTime;

    @Bean(name = "carpoolThreadPool")
    public ThreadPoolExecutor carpoolThreadPool() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(), // 自定义优先级阻塞队列
                new CustomThreadFactory("Carpool-Thread"), // 自定义线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy()  // 饱和策略
        ){
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                System.out.println("Thread " + t.getName() + " is about to execute.");
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    System.err.println("Thread execution error: " + t.getMessage());
                }
            }
        };
    }

    /*

    @Bean(name = "carpoolThreadPool")
    public ThreadPoolExecutor carpoolThreadPool() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity), // 无界队列，任务量大但不确定
                new CustomThreadFactory("Carpool-Thread"), // 使用自定义线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy() // 饱和策略
        );
    }*/


    public static class CustomThreadFactory implements ThreadFactory {
        private final String threadNamePrefix;
        private final AtomicInteger threadCount = new AtomicInteger(1);

        public CustomThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, threadNamePrefix + "-" + threadCount.getAndIncrement());
            thread.setDaemon(false); // 是否为守护线程
            thread.setPriority(Thread.NORM_PRIORITY); // 设置线程优先级
            thread.setUncaughtExceptionHandler((t, e) ->
                    System.err.println("Uncaught exception in thread " + t.getName() + ": " + e.getMessage())
            );
            return thread;
        }
    }
}