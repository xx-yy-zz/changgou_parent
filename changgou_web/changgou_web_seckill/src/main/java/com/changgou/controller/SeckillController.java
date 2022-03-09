package com.changgou.controller;
import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.feign.AddressFeign;
import com.changgou.feign.SeckillFeign;
import com.changgou.feign.WxPayFeign;
import com.changgou.pojo.Order;
import com.changgou.web.CasUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class SeckillController {

    @Autowired
    private SeckillFeign seckillFeign;


    @GetMapping("/index")
    public String index(Model model,String time ){

        //时间场次列表
        Result timeResult = seckillFeign.findListFromRedis();
        List<Map> timeList= (List<Map>)timeResult.getData();

        SimpleDateFormat sdf=new SimpleDateFormat( "HH:mm:ss");
        String now = sdf.format(  new Date() );//获取当前时间
        //循环时间场次
        for(Map map: timeList  ){
            String startTime=  (String)map.get( "startTime" );
            String endTime=  (String)map.get( "endTime" );
            //场次状态
            if( now.compareTo(startTime)>=0  && now.compareTo(endTime)<=0  ){
                map.put( "status" ,"1" );//正在进行中
                if(time==null){  // 如果未传递参数
                    time=(String)map.get( "timeName" );  //指定
                }
                model.addAttribute( "endTime",endTime );  //结束时间
            }else if( now.compareTo(startTime)<0 ){
                map.put( "status" ,"0" );//未开始
            }else{
                map.put( "status" ,"2" );//已结束
            }
            //当前选中的样式处理
            if(time!=null && time.equals( map.get( "name" ) ) ){
                map.put( "class"," active" );
            }else{
                map.put( "class","" );
            }
        }
        model.addAttribute( "timeList",timeList );

        //商品列表
        Result result =  seckillFeign.findSeckillGoodsListFromRedis( time );
        model.addAttribute( "list",result.getData() );

        return "index";
    }


    @Autowired
    private AddressFeign addressFeign;

    /**
     * 秒杀下单(选择收货地址页面)
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/buy")
    public String buy(String skuId,int num,Model model){
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
        Result skuResult = seckillFeign.findBySkuId( skuId );
        Map skuMap = (Map)skuResult.getData();
        Map item=new HashMap(  );
        item.put( "name",skuMap.get( "skuName" ) );
        item.put( "image" ,skuMap.get( "skuImage" ) );
        item.put( "price", skuMap.get( "seckillPrice" ));
        item.put( "num",num );
        Map cart =new HashMap(  );
        cart.put( "checked",true );
        cart.put( "item", item);
        List cartList=new ArrayList(  );
        cartList.add( cart );
        Map cartResult=new HashMap(  );
        cartResult.put( "cartList",cartList );
        cartResult.put( "num",num );
        cartResult.put( "money",  (Integer)skuMap.get( "skuPrice" )*num);
        cartResult.put( "payMoney", (Integer)item.get( "price" )*num);
        cartResult.put( "preMoney",(Integer)skuMap.get( "skuPrice" )*num- (Integer)item.get( "price" )*num);
        model.addAttribute( "cartResult",cartResult );
        model.addAttribute( "skuId",skuId );
        return "order";
    }

    /**
     * 提交订单(校验通过后进行秒杀排队)
     * @param model
     * @param map
     * @return
     */
    @GetMapping("/submit")
    public String submit(  Model model  ,@RequestParam Map map ){
        System.out.println(map);
        map.put( "username",CasUtil.loginName());
        System.out.println( "当前登录用户"+CasUtil.loginName());
        int num = Integer.parseInt( String.valueOf( map.get( "num" ) ) );
        map.put( "num", num );

        Result result = seckillFeign.buy( map );
        if(result.isFlag()){
            Map orderMap =(Map)result.getData();
            int price = Integer.parseInt( String.valueOf( orderMap.get( "seckillPrice" ) ) );
            model.addAttribute( "payMoney",  price*num  ) ;//支付金额
            model.addAttribute( "orderId", orderMap.get( "orderId" ));//订单号
            return "/pay";
        }else{
            model.addAttribute( "message",result.getMessage() );
            return "/fail";
        }
    }


    /**
     * 微信支付
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/wxPay")
    public String wxPay(String orderId, Model model){
        model.addAttribute( "orderId",orderId );
        return "wxpay";
    }


    @Autowired
    private WxPayFeign wxPayFeign;

    /**
     * 获取微信秒杀订单
     * @param orderId
     * @return
     */
    @GetMapping("/seckillOrder/{orderId}")
    @ResponseBody
    public Map seckillOrder(@PathVariable String orderId){
        Result result = wxPayFeign.seckillOrder( orderId );
        return (Map)result.getData();
    }
}
