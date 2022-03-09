package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.util.CanalUtil;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 广告监听类
 */
@CanalEventListener
public class AdListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_business" ,table = {"tb_ad"})
    public void adUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        System.out.println("修改前数据");
        Map<String, String> beforeMap = CanalUtil.convertToMap( rowData.getBeforeColumnsList() );
        System.out.println( JSON.toJSONString( beforeMap ) );
        System.out.println("修改后数据");
        Map<String, String> afterMap = CanalUtil.convertToMap( rowData.getAfterColumnsList() );
        System.out.println( JSON.toJSONString( afterMap ) );

        if( beforeMap!=null &&  !"".equals(  beforeMap.get( "position" )  )  ){
            rabbitTemplate.convertAndSend( "", "ad_update_queue" ,beforeMap.get( "position" ));
        }
        if( afterMap!=null && !"".equals(  afterMap.get( "position" ) )   &&  !afterMap.get( "position" ).equals( beforeMap.get( "position" ) )  ){
            rabbitTemplate.convertAndSend( "", "ad_update_queue" ,afterMap.get( "position" ));
        }


    }


}
