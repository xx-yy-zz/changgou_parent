package com.changgou.order.service;

import com.changgou.pojo.Order;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /***
     * 查询所有
     * @return
     */
    List<Order> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Order findById(String id);

    /***
     * 新增
     * @param order
     */
    Order add(Order order);

    /***
     * 修改
     * @param order
     */
    void update(Order order);

    /***
     * 删除
     * @param id
     */
    void delete(String id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Order> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Order> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Order> findPage(Map<String, Object> searchMap, int page, int size);


    /**
     * 更新订单为已支付状态
     * @param orderId
     * @param transactionId  微信
     */
    void updatePayStatus(String orderId,String transactionId);


    /**
     * 关闭订单
     * @param orderId
     */
    void closeOrder(String orderId);


    /**
     * 订单批量发货
     * @param orderList
     */
    void batchSend(List<Order> orderList);


    /**
     * 确认收货
     * @param orderId
     * @param operator
     */
    void tack(String orderId,String operator);


    /**
     * 自动确认收货
     */
    void autoTack();


    /**
     * 新增秒杀订单
     * @param map
     * @return
     */
    Order addSeckill(Map map);


}
