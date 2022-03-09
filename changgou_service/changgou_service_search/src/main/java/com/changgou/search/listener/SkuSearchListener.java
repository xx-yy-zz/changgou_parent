package com.changgou.search.listener;

import com.changgou.search.service.SkuSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 添加搜索库监听类
 */
@Component
@RabbitListener(queues = "search_add_queue")
public class SkuSearchListener {

    @Autowired
    private SkuSearchService skuSearchService;

    @RabbitHandler
    public void importSkuList(String message){
        System.out.println("接收到消息："+message);
        skuSearchService.importSkuListBySpuId( message );
    }

}
