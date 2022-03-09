package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.feign.SkuFeign;
import com.changgou.order.dao.PreferentialMapper;
import com.changgou.order.service.CartService;
import com.changgou.pojo.OrderItem;
import com.changgou.pojo.Preferential;
import com.changgou.pojo.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    private final String cartKey="cartList";

    private final String collectKey="collectList";//收藏夹

    private final String preKey="preferential";

    @Override
    public Map findCart(String username) {
        //从redis中提取购物车
        List<Map<String,Object>> cartList = (List<Map<String,Object>>)redisTemplate.boundHashOps( cartKey ).get( username );
        if(cartList==null){
            cartList=new ArrayList<>(  );
        }
        Map result=new HashMap(  );
        result.put( "cartList",cartList );

        //计算合计数量与金额
        //获取选中的购物车的列表
        List<OrderItem> orderItemList = cartList.stream()
                .filter( cart -> (boolean) cart.get( "checked" ) )
                .map( cart -> (OrderItem) cart.get( "item" ) )
                .collect( Collectors.toList() );
        int num = orderItemList.stream()
                .mapToInt(OrderItem::getNum ).sum(); //合计数量
        int money = orderItemList.stream()
                .mapToInt( OrderItem::getMoney ).sum();//金额合计
        result.put( "num",num );
        result.put( "money",money );

        //****优惠金额计算****
        //按分类进行分组统计
        Map<Integer, Integer> orderItemGroup = orderItemList.stream()
                .collect( Collectors.groupingBy( OrderItem::getCategoryId3, Collectors.summingInt( OrderItem::getMoney ) ) );
        int preMoney =0; //优惠金额
        for(Integer categoryId: orderItemGroup.keySet()){
            preMoney+=getPreMoney( categoryId, orderItemGroup.get( categoryId ) );
        }
        result.put( "preMoney",preMoney );//优惠金额
        result.put( "payMoney",money-preMoney );//支付金额

        return result;
    }

    @Autowired
    private SkuFeign skuFeign;

    @Override
    public void add(String username, String skuId, int num) {
        //获取购物车
        //从redis中提取购物车
        List<Map<String,Object>> cartList = (List<Map<String,Object>>)redisTemplate.boundHashOps( cartKey ).get( username );
        if(cartList==null){
            cartList=new ArrayList<>(  );
        }

        //轮询购物车中是否存在商品，如果存在，修改数量、金额、重量。。。
        boolean flag=false;//是否存在该商品
        for(Map cart:cartList){

             OrderItem orderItem= (OrderItem)cart.get( "item" );
             if(orderItem.getSkuId().equals( skuId )){
                 int weight = orderItem.getWeight() / orderItem.getNum();  //单位重量
                 orderItem.setNum(  orderItem.getNum()+num );//修改数量
                 orderItem.setMoney( orderItem.getPrice()*orderItem.getNum() );//修改金额
                 orderItem.setWeight( weight*orderItem.getNum()   );//总重量
                 if(orderItem.getNum()<=0){
                     cartList.remove( cart );
                 }
                 flag=true;
                 break;
             }
        }

        //如果不存在，新增
        if(!flag){
            if(num<=0){
                throw  new RuntimeException( "数量非法" );
            }
            Result skuResult = skuFeign.findById( skuId );
            if(skuResult.getData()==null){
                throw  new RuntimeException( "商品不存在" );
            }
            Sku sku = JSON.parseObject( JSON.toJSONString( skuResult.getData() ), Sku.class );
            if(!"1".equals(sku.getStatus()  )){
                throw  new RuntimeException( "商品状态非法" );
            }
            OrderItem orderItem=new OrderItem();
            orderItem.setSpuId( sku.getSpuId()  );
            orderItem.setSkuId( skuId );
            orderItem.setName( sku.getName() );
            orderItem.setPrice( sku.getPrice() );
            orderItem.setNum( num );
            orderItem.setMoney( sku.getPrice()*num );
            orderItem.setWeight( sku.getWeight()*num );
            orderItem.setCategoryId3( sku.getCategoryId() );
            orderItem.setImage( sku.getImage() );
            Map map=new HashMap(  );
            map.put( "item",orderItem );
            map.put( "checked" ,true);
            cartList.add( map );
        }
        //保存购物车
        redisTemplate.boundHashOps( cartKey ).put( username,cartList );

    }

    @Override
    public void updateChecked(String username, String skuId, boolean checked) {
        //获取购物车
        //从redis中提取购物车
        List<Map<String,Object>> cartList = (List<Map<String,Object>>)redisTemplate.boundHashOps( cartKey ).get( username );
        if(cartList==null){
            return;
        }
        for(Map cart:cartList){
            OrderItem orderItem= (OrderItem)cart.get( "item" );
            if(orderItem.getSkuId().equals( skuId )){
                cart.put( "checked", checked);
            }
        }
        //保存购物车
        redisTemplate.boundHashOps( cartKey ).put( username,cartList );


    }

    @Override
    public void deleteChecked(String username) {
        //从redis中提取购物车
        List<Map<String,Object>> cartList = (List<Map<String,Object>>)redisTemplate.boundHashOps( cartKey ).get( username );
        if(cartList==null){
            return;
        }
        //筛选未选择的购物车
        List<Map<String, Object>> noCheckedCartList = cartList.stream()
                .filter( cart -> !(boolean) cart.get( "checked" ) )
                .collect( Collectors.toList() );
        //将未选择的购物车覆盖购物车列表以达到删除选中的购物车的目的！
        redisTemplate.boundHashOps( cartKey ).put( username, noCheckedCartList);
    }

    @Override
    public void collectChecked(String username) {
        //从redis中提取购物车
        List<Map<String,Object>> cartList = (List<Map<String,Object>>)redisTemplate.boundHashOps( cartKey ).get( username );
        if(cartList==null){
            return;
        }
        //选中的购物车
        List<Map<String, Object>> checkedCartList = cartList.stream()
                .filter( cart -> (boolean) cart.get( "checked" ) )
                .collect( Collectors.toList() );

        //获取原来的收藏夹的数据
        List<Map<String, Object>> collectList = (List<Map<String, Object>>)redisTemplate.boundHashOps( collectKey ).get( username );
        if(collectList==null){
            collectList=new ArrayList<>(  );
        }
        collectList.addAll( checkedCartList );//追加数据

        redisTemplate.boundHashOps( collectKey ).put( username, collectList);//放入收藏夹

        //删除选中的购物车
        deleteChecked( username );
    }

    @Override
    public List<Map> refreshCartList(String username) {
        //从redis中提取购物车
        List<Map> cartList = (List<Map>)redisTemplate.boundHashOps( cartKey ).get( username );
        if(cartList==null){
           return new ArrayList<>(  );
        }

        //循环购物车，获取最新价格
        for(int i=0;i<cartList.size();i++){
            Map cart = (Map)cartList.get( i );
            OrderItem orderItem= (OrderItem)cart.get( "item" );
            String skuId = orderItem.getSkuId();
            Result skuResult = skuFeign.findById( skuId );
            Map sku=(Map)skuResult.getData();
            if(sku!=null){
                if("1".equals( (String)sku.get( "status" ) )  ){//正常
                    orderItem.setPrice( (Integer) sku.get( "price" ) );//刷新价格
                    orderItem.setMoney(  orderItem.getPrice()*orderItem.getNum()  );//刷新金额
                }else{
                    cart.put( "checked",false );//取消选中
                }
            }else{
               cartList.remove( cart );//从购物车中移除
            }
        }
        //存回redis
        redisTemplate.boundHashOps( cartKey ).put( username,cartList );
        return cartList;
    }

    @Autowired
    private PreferentialMapper preferentialMapper;

    /**
     * 根据分类id和消费额 查询优惠规则表
     * @param categoryId
     * @param money
     * @return
     */
    private List<Preferential> findPreferentialList(Integer categoryId,int money){
        //从数据库加载优惠规则表
        List<Preferential> preferentialList = (List<Preferential>)redisTemplate.boundValueOps( preKey ).get();
        if(preferentialList==null){
            preferentialList = preferentialMapper.selectAll();
            redisTemplate.boundValueOps( preKey ).set(preferentialList  );
        }

        //筛选数据(包括翻倍和不翻倍)
        return preferentialList.stream()
                .filter( preferential -> "1".equals( preferential.getState() ) )//状态
                .filter( preferential -> preferential.getCategoryId().equals( categoryId ) )//分类
                .filter( preferential -> preferential.getBuyMoney() <= money ) //消费额度
                .filter( preferential -> preferential.getStartTime().before( new Date() ) )
                .filter( preferential -> preferential.getEndTime().after( new Date() ) )
                .collect( Collectors.toList() );

    }


    /**
     * 计算不翻倍的优惠金额
     * @param preferentialList
     * @return
     */
    private int getPreMoney1(List<Preferential> preferentialList){

        List<Preferential> preferentialList1 = preferentialList.stream()
                .filter( preferential -> "1".equals( preferential.getType() ) )//类型为不翻倍
                .sorted( Comparator.comparing( Preferential::getBuyMoney ).reversed() ) //排序
                .limit( 1 )//取第一条
                .collect( Collectors.toList() );
        if(preferentialList1.size()>0){
            return preferentialList1.get( 0 ).getPreMoney();
        }else{
            return 0;
        }
    }


    /**
     * 计算翻倍的优惠金额
     * @param preferentialList
     * @return
     */
    private int getPreMoney2(List<Preferential> preferentialList,int money){

        List<Preferential> preferentialList1 = preferentialList.stream()
                .filter( preferential -> "2".equals( preferential.getType() ) )//类型为翻倍
                .sorted( Comparator.comparing( Preferential::getBuyMoney ).reversed() ) //排序
                .limit( 1 )//取第一条
                .collect( Collectors.toList() );
        if(preferentialList1.size()>0){
            Preferential preferential = preferentialList1.get( 0 );
            int multiple =money/ preferential.getBuyMoney(); //计算倍数
            return preferential.getPreMoney()*multiple;
        }else{
            return 0;
        }
    }


    /**
     * 计算优惠金额
     * @param categoryId 分类
     * @param money 消费金额
     * @return
     */
    private int getPreMoney(Integer categoryId,int money){
        //优惠规则表
        List<Preferential> preferentialList = findPreferentialList( categoryId, money );
        int preMoney1 = getPreMoney1( preferentialList ); //不翻倍的优惠额度
        int preMoney2 = getPreMoney2( preferentialList, money );//翻倍的优惠额度
        return preMoney1>preMoney2?preMoney1:preMoney2;  //返回最大的
    }


}
