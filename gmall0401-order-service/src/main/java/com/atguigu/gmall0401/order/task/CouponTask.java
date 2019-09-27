package com.atguigu.gmall0401.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-27 19:36
 */
//@Component
//@EnableScheduling
public class CouponTask {

    @Reference
    OrderService orderService;


//    @Scheduled(cron = "0/5 * * * * ?")  // 每五秒执行一次
    public void work() throws InterruptedException {
        System.out.println("thread = ===============" + Thread.currentThread());

        List<Integer> integers = orderService.checkExpiredCoupon();
        for (Integer couponId : integers) {
            orderService.handleExpiredCoupon(couponId);
        }

    }


    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        return taskScheduler;
    }



}
