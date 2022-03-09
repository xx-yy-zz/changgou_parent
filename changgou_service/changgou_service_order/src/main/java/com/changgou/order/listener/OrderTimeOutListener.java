package com.changgou.order.listener;

import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "queue.ordertimeout")
public class OrderTimeOutListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void closeOrder(String orderId){
        System.out.println("收到关闭订单消息"+orderId);
        orderService.closeOrder( orderId );
    }


}
