package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WxPayService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀支付监听
 */
@Component
@RabbitListener(queues = "seckill_pay")
public class SeckillPayListener {

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @RabbitHandler
    public  void seckillPay(String message){
        Map order = JSON.parseObject( message );
        //调用微信支付
        Map map = wxPayService.nativePay( (String) order.get( "id" ),(Integer) order.get( "payMoney" ));
        Map data= new HashMap(  );
        data.put( "orderId",(String) order.get( "id" ) );//订单号
        data.put( "code_url",  map.get( "code_url" ) );//支付二维码url
        data.put( "payMoney",(Integer) order.get( "payMoney" ) );
        //存入redis
        redisTemplate.boundHashOps( "seckill_order" ).put((String) order.get( "id" ), data );
        //发送到mq
        rabbitTemplate.convertAndSend( "seckill_pay",(String) order.get( "id" ), JSON.toJSONString( data ) );
        System.out.println("发送到seckill_pay交换器的数据:"+ JSON.toJSONString( data ));
    }
}
