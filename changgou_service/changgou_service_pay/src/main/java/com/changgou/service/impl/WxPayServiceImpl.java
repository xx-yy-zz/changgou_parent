package com.changgou.service.impl;

import com.changgou.service.WxPayService;
import com.github.wxpay.sdk.MyConfig;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private WXPay wxPay;


    @Value( "${wxpay.notify_url}" )
    private String notifyUrl;//回调地址

    /**
     * 统一下单
     * @param orderId
     * @param money
     * @return
     */
    @Override
    public Map nativePay(String orderId, Integer money) {

        try {
            Map<String,String> map=new HashMap(  );
            map.put("body","畅购商城");
            map.put( "out_trade_no",orderId );
            map.put( "notify_url",notifyUrl );
            map.put( "trade_type","NATIVE" );
            map.put( "total_fee", String.valueOf( money )  );
            map.put( "spbill_create_ip","127.0.0.1" );
            Map<String, String> result = wxPay.unifiedOrder( map );
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Map queryOrder(String orderId) {
        Map map=new HashMap(  );
        map.put( "out_trade_no",orderId );
        try {
            return wxPay.orderQuery( map );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closeOrder(String orderId) {
        Map map=new HashMap(  );
        map.put( "out_trade_no",orderId );
        try {
            return wxPay.closeOrder( map );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
