package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.feign.SeckillFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private SeckillFeign seckillFeign;

    /**
     * 查询秒杀商品
     * @return
     */
    @GetMapping("/{skuId}")
    public Result findSeckillGoods(@PathVariable String skuId){
        return seckillFeign.findBySkuId( skuId );
    }

}