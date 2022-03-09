package com.changgou.seckill.listener;

import com.changgou.pojo.SeckillGoods;
import com.changgou.seckill.service.SeckillGoodsService;
import com.changgou.seckill.service.SeckillTimeService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@RabbitListener(queues = "seckill_task")
public class SeckillListener {

    @Autowired
    private SeckillTimeService seckillTimeService;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @RabbitHandler
    public void seckillToRedis(){
        //时间段数据载入缓存
        seckillTimeService.findListToRedis();
        //秒杀商品数据载入缓存
        SimpleDateFormat sdf=new SimpleDateFormat( "yyyy-MM-dd" );
        String date=sdf.format( new Date() );// 当前日期
        System.out.println("开始导入"+date+"的秒杀商品数据");
        seckillGoodsService.saveListToRedis( date );
    }
}
