package com.changgou.search.listener;

import com.changgou.search.service.SkuSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 批量导入
 */
@Component
public class ImportAll {

    @Autowired
    private SkuSearchService skuSearchService;

    @RabbitListener(queues = "search_add_all_queue")
    public void importAll(String message){
        System.out.println("接收到消息："+message);
        //if(message.equals( "1" )){
        skuSearchService.importAll();
        //}
    }

}
