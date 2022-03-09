package com.changgou.page.listener;

import com.changgou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "page_create_queue")
public class PageListener {

    @Autowired
    private PageService pageService;

    @RabbitHandler
    public void createPage(String message){
        System.out.println("接收到消息："+message);
        pageService.createPage( message );
    }

}
