package com.changgou.order.listener;

import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "order_tack")
public class OrderAutoTackListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void autoTack(String message){
        orderService.autoTack();
    }

}
