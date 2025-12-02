package com.huike.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableAsync //开启异步任务
@Configuration
public class ThreadPoolConfig {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = null;
    private ThreadPoolExecutor threadPoolExecutor = null;

    @Bean("jdkThreadPool")
    public ThreadPoolExecutor threadPoolExecutor(){
        log.info("初始化项目的线程池 (原始) .... ");
        threadPoolExecutor = new ThreadPoolExecutor(10,
                20,
                5,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        return threadPoolExecutor;
    }


    @Bean("springThreadPool")
    public ThreadPoolTaskExecutor taskExecutor(){
        log.info("初始化项目的线程池 (spring) .... ");
        threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setKeepAliveSeconds(300);
        threadPoolTaskExecutor.setThreadNamePrefix("Huike-Thread-Pool-");
        threadPoolTaskExecutor.setQueueCapacity(10);
        return threadPoolTaskExecutor;
    }



   @PreDestroy
   public void shutdown(){
       log.info("服务器关闭, 释放资源,  关闭线程池");
       if(threadPoolExecutor != null){
           log.info("服务器关闭, 释放资源,  关闭线程池threadPoolExecutor");
           threadPoolExecutor.shutdown();
       }
       if(threadPoolTaskExecutor != null){
           log.info("服务器关闭, 释放资源,  关闭线程池threadPoolTaskExecutor");
           threadPoolTaskExecutor.shutdown();
       }
   }

}
