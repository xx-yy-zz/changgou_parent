package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillGoodsService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 秒杀库存扣减
 */
@Component
@RabbitListener(queues = "seckill_goods")
public class SeckillGoodsListener {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @RabbitHandler
    public void deduction(String message){
        Map orderMap = JSON.parseObject( message );
        seckillGoodsService.deduction(orderMap);
    }
}
