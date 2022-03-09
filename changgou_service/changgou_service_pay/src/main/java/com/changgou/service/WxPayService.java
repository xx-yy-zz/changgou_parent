package com.changgou.service;

import java.util.Map;

/**
 * 微信支付接口
 */
public interface WxPayService {

    /**
     * 生成微信支付二维码（下单）
     * @param orderId
     * @param money
     * @return
     */
    Map nativePay(String orderId, Integer money);


    /**
     * 查询订单
     * @param orderId
     * @return
     */
    Map queryOrder(String orderId);


    /**
     * 关闭订单
     * @param orderId
     * @return
     */
    Map closeOrder(String orderId);

}
