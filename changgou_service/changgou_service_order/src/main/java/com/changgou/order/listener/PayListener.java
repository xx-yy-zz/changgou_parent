package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "order_pay")
public class PayListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void updatePayStatus(String message){
        System.out.println("接收到消息"+message);
        Map<String,String> map = JSON.parseObject( message, Map.class );

        orderService.updatePayStatus(map.get( "orderId" ),map.get( "transactionId" )  );

    }

}
