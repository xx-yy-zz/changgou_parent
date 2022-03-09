package com.changgou.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "seckill")
public interface SeckillFeign {


    /**
     * 从缓存中读取秒杀商品
     * @param time 时间段
     */
    @GetMapping("/seckillGoods/listtime/time/{time}")
    public Result findSeckillGoodsListFromRedis(@PathVariable("time") String time);


    /**
     * 从缓存中查询时间段列表
     * @return
     */
    @GetMapping("/seckillTime/list")
    public Result findListFromRedis();


    /***
     * 根据ID查询数据
     * @param skuId
     * @return
     */
    @GetMapping("/seckillGoods/sku/{skuId}")
    public Result findBySkuId(@PathVariable("skuId") String skuId);



    /**
     * 秒杀下单
     * @param map
     * @return
     */
    @PostMapping("/seckillGoods/buy")
    public Result buy(@RequestBody Map map);


}
