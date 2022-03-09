package com.changgou.task;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
/*
@Component
public class OrderTask {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Scheduled(cron = "0 0 0 * * ?")
    public void autoTack(){
        System.out.println(new Date(  ) );
        rabbitTemplate.convertAndSend( "","order_tack","-" );
    }


}
*/