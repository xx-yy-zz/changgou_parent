package com.changgou.order.listener;

import com.changgou.order.dao.PreferentialMapper;
import com.changgou.pojo.Preferential;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RabbitListener(queues = "tb_preferential_update")
public class PreListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PreferentialMapper preferentialMapper;

    private final String preKey="preferential";

    @RabbitHandler
    public void preUpdate(String message){
        System.out.println("接收到消息,更新preferential缓存");
        List<Preferential> preferentials = preferentialMapper.selectAll();
        redisTemplate.boundValueOps(preKey  ).set( preferentials );
    }

}
