package com.changgou.seckill.dao;

import com.changgou.pojo.SeckillGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SeckillGoodsMapper extends Mapper<SeckillGoods> {



    /**
     * 根据日期查询秒杀商品
     * @param date
     * @return
     */
    @Select( "SELECT g.id,sku_id skuId,seckill_price  seckillPrice,seckill_num  seckillNum,seckill_surplus seckillSurplus,seckill_limit seckillLimit, " +
            "t.`id` timeId, t.`name` timeName,sku_name skuName,sku_sn skuSn,sku_price skuPrice,sku_image skuImage ,seq ,start_time startTime,end_time endTime " +
            "FROM tb_seckill_time t, tb_seckill_goods g,tb_seckill_activity a " +
            "WHERE g.`time_id`=t.`id` AND g.`activity_id`=a.`id` AND a.`startDate`<=#{date} AND a.`endDate`>=#{date} " +
            "ORDER BY seq " )
    public List<Map> findListByDate(@Param( "date" ) String date);

}
