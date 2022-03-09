package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.feign.OrderFeign;
import com.changgou.feign.WxPayFeign;
import com.changgou.pojo.Order;
import com.changgou.web.CasUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class PayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private WxPayFeign wxPayFeign;


    /**
     * 微信支付
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/wxPay")
    public String wxPay(String orderId, Model model){
        Result orderResult = orderFeign.findById( orderId );
        if(orderResult.getData()==null){
            return "fail";
        }
        Order order = JSON.parseObject( JSON.toJSONString( orderResult.getData() ), Order.class );
        //判断是当前用户的订单
        if(!order.getUsername().equals( CasUtil.loginName() ) ){
            return "fail";
        }
        //判断是未支付的订单
        if( !"0".equals( order.getPayStatus() ) ){
            return "fail";
        }
        Integer payMoney = order.getPayMoney();
        //调用支付微服务，得到结果
        Result payResult = wxPayFeign.nativePay( orderId, payMoney );
        Map payMap = (Map)payResult.getData();
        if(  "SUCCESS".equals( payMap.get( "result_code" ) ) ){
            model.addAllAttributes( payMap );
            model.addAttribute( "orderId",orderId );
            model.addAttribute( "payMoney" ,payMoney );
            return "wxpay";
        }else{
            return "fail";
        }
    }

}
