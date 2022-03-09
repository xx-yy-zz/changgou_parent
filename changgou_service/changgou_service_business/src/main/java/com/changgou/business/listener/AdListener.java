package com.changgou.business.listener;

import com.changgou.business.service.AdService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 广告消息监听
 */
@Component
@RabbitListener(queues = "ad_update_queue")
public class AdListener {

    @Autowired
    private AdService adService;

    @RabbitHandler
    public void adUpdate(String message){
        System.out.println("接收到消息："+message);
        adService.updateToRedis(message);
    }

}
