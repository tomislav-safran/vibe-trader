package com.tsafran.vibetrader.trade;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class TradeSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler tradeTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("trade-scheduler-");
        return scheduler;
    }
}
