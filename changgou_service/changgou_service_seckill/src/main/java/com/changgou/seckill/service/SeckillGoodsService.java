package com.changgou.seckill.service;

import com.changgou.pojo.SeckillGoods;
import com.github.pagehelper.Page;
import java.util.List;
import java.util.Map;

public interface SeckillGoodsService {

    /***
     * 查询所有
     * @return
     */
    List<SeckillGoods> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    SeckillGoods findById(Long id);

    /***
     * 新增
     * @param seckillGoods
     */
    void add(SeckillGoods seckillGoods);

    /***
     * 修改
     * @param seckillGoods
     */
    void update(SeckillGoods seckillGoods);

    /***
     * 删除
     * @param id
     */
    void delete(Long id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<SeckillGoods> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<SeckillGoods> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<SeckillGoods> findPage(Map<String, Object> searchMap, int page, int size);


    /**
     * 根据日期查询秒杀商品
     * @param date 日期
     * @return
     */
    List<Map> findListByDate(String date);


    /**
     * 将某日的秒杀列表加载到缓存
     * @param date
     */
    void saveListToRedis(String date);


    /**
     * 从缓存中读取秒杀商品
     * @param time 时间段
     */
    List<Map> findListFromRedis(String time);

    /**
     * 根据skuID查询秒杀商品
     * @param skuId
     * @return
     */
    Map findBySkuid(String skuId);


    /**
     * 秒杀下单
     * @param orderMap
     * @return
     */
    String buy(Map orderMap);

    /**
     * 秒杀商品库存扣减
     * @param orderMap
     * @return
     */
    boolean deduction(Map orderMap);



}
