package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.feign.AddressFeign;
import com.changgou.feign.CartFeign;
import com.changgou.feign.OrderFeign;
import com.changgou.pojo.Order;
import com.changgou.web.CasUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {


    @Autowired
    private AddressFeign addressFeign;


    @Autowired
    private CartFeign cartFeign;

    /**
     * 结算页 显示购物清单与地址信息
     * @param model
     * @return
     */
    @GetMapping("/order")
    public String order(Model model){
        Map map=new HashMap(  );
        map.put( "username", CasUtil.loginName() );
        //查询地址列表
        Result result = addressFeign.findList( map );
        model.addAttribute( "addressList",result.getData() );

        //获取默认地址
        List<Map> addressList = (List<Map>)result.getData();
        Map defaultAddress=null; //默认地址
        for(Map address:addressList){
            if( "1".equals( address.get( "isDefault" ) ) ){
                defaultAddress= address;
            }
        }
        if(defaultAddress==null && addressList.size()>0){
            defaultAddress=addressList.get( 0 );
        }
        model.addAttribute( "defaultAddress",defaultAddress );

        //购物清单
        Result cartResult = cartFeign.findCart(  CasUtil.loginName() );
        model.addAttribute( "cartResult",cartResult.getData() );

        return "order";
    }


    @Autowired
    private OrderFeign orderFeign;

    /**
     * 提交订单
     * @param model
     * @param ,@ModelAttribute Order order
     * @return
     */
    @GetMapping("/submit")
    public String submit(  Model model  ,@ModelAttribute Order order ){
        order.setUsername(  CasUtil.loginName() );
        order.setSourceType( "1" );  //订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
        Result result = orderFeign.add( order );
        if(result.isFlag()){
            Map orderMap =(Map)result.getData();
            model.addAttribute( "payMoney", orderMap.get( "payMoney" ));//支付金额
            model.addAttribute( "orderId", orderMap.get( "id" ));//订单号
            if(order.getPayType().equals( "1" )){  //在线支付
                return "/pay";
            }else {
                return "/success";
            }
        }else{
            return "/fail";
        }
    }

}
