package com.changgou.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.util.CanalUtil;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.UpdateListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@CanalEventListener
public class SpuListener {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @UpdateListenPoint(schema = "changgou_goods",table = {"tb_spu"})
    public void spuUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        //获取修改前数据
        Map<String, String> beforeMap = CanalUtil.convertToMap( rowData.getBeforeColumnsList() );
        //获取修改后数据
        Map<String, String> afterMap = CanalUtil.convertToMap( rowData.getAfterColumnsList() );

        //上架操作判断
        if( beforeMap.get( "is_marketable" ).equals( "0" )  &&  afterMap.get( "is_marketable" ).equals( "1" ) ){
            System.out.println("上架： 发送到交换器goods_up_exchange："+ afterMap.get( "id" ) );
            rabbitTemplate.convertAndSend( "goods_up_exchange","", afterMap.get( "id" ) );
        }


    }

}
