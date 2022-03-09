package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.service.WxPayService;
import com.changgou.util.ConvertUtils;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wxpay")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;

    /**
     * 统一下单
     * @param orderId
     * @param money
     * @return
     */
    @GetMapping("/nativePay")
    public Result nativePay(String orderId,Integer money){
        Map map = wxPayService.nativePay( orderId, money );
        return new Result( true, StatusCode.OK,"",map );
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 回调
     */
    @RequestMapping("/notify")
    public void notifyLogic(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("支付回调");

        InputStream inputStream = request.getInputStream();
        String result = ConvertUtils.convertToString( inputStream );
        System.out.println(result);

        try {
            Map<String, String> map = WXPayUtil.xmlToMap( result );

            if("SUCCESS".equals( map.get( "result_code" ) )){  //如果结果成功,你也不要轻易相信
                Map map1 = wxPayService.queryOrder( map.get( "out_trade_no" ));  //查询微信支付平台
                if("SUCCESS".equals( map1.get(  "trade_state") )){  //判断支付状态
                    //修改订单状态
                    Map map2=new HashMap(  );
                    map2.put( "orderId",map.get( "out_trade_no" ) );
                    map2.put( "transactionId", map.get( "transaction_id" ));

                    rabbitTemplate.convertAndSend( "","order_pay", JSON.toJSONString(map2) );

                    rabbitTemplate.convertAndSend( "paynotify","", map.get( "out_trade_no" ));
                    System.out.println("支付成功，发送到队列");
                    response.setContentType( "text/xml" );
                    response.getWriter().write( "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>" );
                }else{
                    System.out.println("收到支付成功消息，但消息可能是伪造的！");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }




    }


    /**
     * 关闭订单
     * @param orderId
     * @return
     */
    @PutMapping("/close/{orderId}")
    public Result closeOrder(@PathVariable String orderId){
        Map map = wxPayService.closeOrder( orderId );
        return new Result( true,StatusCode.OK,"" ,map);
    }


    /**
     * 查询订单
     * @param orderId
     * @return
     */
    @PutMapping("/query/{orderId}")
    public Result queryOrder(@PathVariable String orderId){
        Map map = wxPayService.queryOrder( orderId );
        return new Result( true,StatusCode.OK,"" ,map);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取秒杀订单
     * @param orderId
     * @return
     */
    @GetMapping("/seckillOrder/{orderId}")
    public Result seckillOrder(@PathVariable String orderId){
        Map seckill_order = (Map)redisTemplate.boundHashOps( "seckill_order" ).get( orderId );
        return new Result( true,StatusCode.OK,"",seckill_order );
    }

}
