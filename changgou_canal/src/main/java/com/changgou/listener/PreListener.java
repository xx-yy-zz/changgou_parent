package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.util.CanalUtil;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 优惠规则监听类
 */
@CanalEventListener
public class PreListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_order" ,table = {"tb_preferential"})
    public void preUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        System.out.println("tb_preferential发生变更");
        rabbitTemplate.convertAndSend( "","tb_preferential_update","-" );
    }


}
