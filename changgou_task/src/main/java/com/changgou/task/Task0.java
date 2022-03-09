package com.changgou.task;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Task0 {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 每天凌晨执行的任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void task0(){
        //发送到零点任务交换器
        rabbitTemplate.convertAndSend( "exchange.task0","","0" );
    }

}
