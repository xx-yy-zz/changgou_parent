package com.changgou.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("pay")
public interface WxPayFeign {

    /**
     * 统一下单
     * @param orderId
     * @param money
     * @return
     */
    @GetMapping("/wxpay/nativePay")
    public Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("money") Integer money);



    /**
     * 关闭订单
     * @param orderId
     * @return
     */
    @PutMapping("/wxpay/close/{orderId}")
    public Result closeOrder(@PathVariable("orderId") String orderId);


    /**
     * 查询订单
     * @param orderId
     * @return
     */
    @PutMapping("/wxpay/query/{orderId}")
    public Result queryOrder(@PathVariable("orderId") String orderId);


    /**
     * 获取秒杀订单
     * @param orderId
     * @return
     */
    @GetMapping("/wxpay/seckillOrder/{orderId}")
    public Result seckillOrder(@PathVariable("orderId") String orderId);

}
