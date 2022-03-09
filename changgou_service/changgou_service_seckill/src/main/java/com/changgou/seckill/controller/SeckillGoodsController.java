package com.changgou.seckill.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.service.SeckillGoodsService;
import com.changgou.pojo.SeckillGoods;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {


    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",seckillGoodsList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id){
        SeckillGoods seckillGoods = seckillGoodsService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",seckillGoods);
    }


    /***
     * 新增数据
     * @param seckillGoods
     * @return
     */
    @PostMapping
    public Result add(@RequestBody SeckillGoods seckillGoods){
        seckillGoodsService.add(seckillGoods);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param seckillGoods
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody SeckillGoods seckillGoods,@PathVariable Long id){
        seckillGoods.setId(id);
        seckillGoodsService.update(seckillGoods);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        seckillGoodsService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<SeckillGoods> list = seckillGoodsService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<SeckillGoods> pageList = seckillGoodsService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


    /**
     * 将当天的秒杀数据装入redis
     */
    @GetMapping("/seckillToRedis")
    public void seckillToRedis(){
        //获取当前日期字符串
        SimpleDateFormat sdf=new SimpleDateFormat( "yyyy-MM-dd" );
        String date=sdf.format( new Date() );// 当前日期
        System.out.println("开始导入"+date+"的秒杀商品数据");
        seckillGoodsService.saveListToRedis( date );
    }


    /**
     * 从缓存中读取秒杀商品
     * @param time 时间段
     */
    @GetMapping("/listtime/time/{time}")
    public Result findListFromRedis(@PathVariable String time){
        List<Map> list = seckillGoodsService.findListFromRedis( time );
        return new Result( true,StatusCode.OK,"",list );
    }


    /***
     * 根据ID查询数据
     * @param skuId
     * @return
     */
    @GetMapping("/sku/{skuId}")
    public Result findBySkuId(@PathVariable String skuId){
        Map map = seckillGoodsService.findBySkuid( skuId );
        return new Result(true,StatusCode.OK,"查询成功",map);
    }

    /**
     * 秒杀下单
     * @param map
     * @return
     */
    @PostMapping("/buy")
    public Result buy(@RequestBody Map map){
        String orderId = seckillGoodsService.buy(map);
        map.put( "orderId",orderId );
        return new Result( true,StatusCode.OK,"加入秒杀下单排队" ,map);
    }


}
