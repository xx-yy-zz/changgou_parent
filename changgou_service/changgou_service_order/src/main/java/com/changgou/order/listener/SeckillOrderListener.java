package com.changgou.order.listener;
import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RabbitListener(queues = "seckill_order")
public class SeckillOrderListener {

    @Autowired
    private OrderService orderService;

    /**
     * 创建秒杀订单
     */
    @RabbitHandler
    public void createSeckillOrder(String message){
        Map map = JSON.parseObject( message );
        orderService.addSeckill( map  );
    }

}