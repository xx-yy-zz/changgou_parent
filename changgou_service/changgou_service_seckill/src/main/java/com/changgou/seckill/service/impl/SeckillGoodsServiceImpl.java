package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.service.SeckillGoodsService;
import com.changgou.pojo.SeckillGoods;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<SeckillGoods> findAll() {
        return seckillGoodsMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public SeckillGoods findById(Long id){
        return  seckillGoodsMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param seckillGoods
     */
    @Override
    public void add(SeckillGoods seckillGoods){
        seckillGoodsMapper.insert(seckillGoods);
    }


    /**
     * 修改
     * @param seckillGoods
     */
    @Override
    public void update(SeckillGoods seckillGoods){
        seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillGoodsMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<SeckillGoods> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return seckillGoodsMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<SeckillGoods> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<SeckillGoods>)seckillGoodsMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<SeckillGoods> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<SeckillGoods>)seckillGoodsMapper.selectByExample(example);
    }

    @Override
    public List<Map> findListByDate(String date) {
        return seckillGoodsMapper.findListByDate(date);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(SeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // skuId
            if(searchMap.get("sku_id")!=null && !"".equals(searchMap.get("sku_id"))){
                criteria.andLike("sku_id","%"+searchMap.get("sku_id")+"%");
           	}
            // sku商品名称
            if(searchMap.get("sku_name")!=null && !"".equals(searchMap.get("sku_name"))){
                criteria.andLike("sku_name","%"+searchMap.get("sku_name")+"%");
           	}
            // sn
            if(searchMap.get("sku_sn")!=null && !"".equals(searchMap.get("sku_sn"))){
                criteria.andLike("sku_sn","%"+searchMap.get("sku_sn")+"%");
           	}
            // 秒杀商品图片
            if(searchMap.get("sku_image")!=null && !"".equals(searchMap.get("sku_image"))){
                criteria.andLike("sku_image","%"+searchMap.get("sku_image")+"%");
           	}

            // 秒杀价格
            if(searchMap.get("seckillPrice")!=null ){
                criteria.andEqualTo("seckillPrice",searchMap.get("seckillPrice"));
            }
            // 秒杀数量
            if(searchMap.get("seckillNum")!=null ){
                criteria.andEqualTo("seckillNum",searchMap.get("seckillNum"));
            }
            // 剩余数量
            if(searchMap.get("seckillSurplus")!=null ){
                criteria.andEqualTo("seckillSurplus",searchMap.get("seckillSurplus"));
            }
            // 限购数量
            if(searchMap.get("seckillLimit")!=null ){
                criteria.andEqualTo("seckillLimit",searchMap.get("seckillLimit"));
            }
            // 秒杀时间段id
            if(searchMap.get("timeId")!=null ){
                criteria.andEqualTo("timeId",searchMap.get("timeId"));
            }
            // 原价格
            if(searchMap.get("skuPrice")!=null ){
                criteria.andEqualTo("skuPrice",searchMap.get("skuPrice"));
            }
            // 排序
            if(searchMap.get("seq")!=null ){
                criteria.andEqualTo("seq",searchMap.get("seq"));
            }

        }
        return example;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private final String listKey="secckill"; //秒杀商品

    @Override
    public void saveListToRedis(String date) {
        List<Map> seckillGoodsList = seckillGoodsMapper.findListByDate( date );
        System.out.println("秒杀商品列表："+ JSON.toJSONString(seckillGoodsList) );
        for(Map map:seckillGoodsList){
            //将秒杀商品对象放入缓存
            redisTemplate.boundHashOps(listKey ).put( (String)map.get( "skuId" ),map  );
        }
    }


    @Override
    public List<Map> findListFromRedis(String time) {
        if(time==null){
            return new ArrayList<>(  );
        }
        List<Map> list = redisTemplate.boundHashOps( listKey ).values();
        System.out.println("秒杀列表：" + JSON.toJSONString( list ) );
        if(list==null){
            return new ArrayList<>(  );
        }
        return  list.stream()
                .filter( map -> time.equals( map.get( "timeName" ) ) )
                .collect( Collectors.toList() );
    }


    @Override
    public Map findBySkuid(String skuId) {
        return (Map)redisTemplate.boundHashOps( listKey  ).get( skuId );
    }




    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IdWorker idWorker;

    @Override
    public String buy(Map orderMap) {
        String username=(String)orderMap.get( "username" );
        String skuId=(String)orderMap.get( "skuId" );
        int num=  Integer.parseInt(  String.valueOf(orderMap.get( "num" ))) ;//获得购买数量
        if(orderMap.get( "num" )==null){
            orderMap.put( "num",1 );//默认为1
        }

        //1.根据skuId查询秒杀商品信息
        Map skuMap = findBySkuid( skuId );
        if(skuMap==null){
            throw new RuntimeException( "无此商品");
        }
        System.out.println("1 ok!");
        //2.判断是否在秒杀时间段
        SimpleDateFormat sdf=new SimpleDateFormat( "HH:mm:ss");//获取当前时间
        String now = sdf.format(  new Date() );//获取当前时间
        if(now.compareTo( (String)skuMap.get( "startTime" ) )<0 ){
            throw new RuntimeException( "秒杀活动未开始");
        }
        if(now.compareTo( (String)skuMap.get( "endTime" ) )>0 ){
            throw new RuntimeException( "秒杀活动已结束");
        }
        System.out.println("2 ok!");
        //3.判断秒杀商品库存是否充足
        int surplus = (Integer) skuMap.get( "seckillSurplus" );  //商品的库存数量
        if(surplus<num){  //如果库存不足
            throw new RuntimeException( "手太慢了，商品已经被秒光");
        }
        System.out.println("3 ok!");
        //4.判断该用户是否超过限购数量
        int limitBuyNum = limitBuyNum( username,skuId);  //该用户可购买的数量
        if(num>limitBuyNum){  //如果用户超过可购买的数量，返回异常
            throw new RuntimeException( "你已经买过该商品了，把机会留给别人吧");
        }
        System.out.println("4 ok!");
        //5.产生订单号 发送到mq
        String orderId = idWorker.nextId()+""; //订单号
        orderMap.put( "orderId",orderId );
        orderMap.putAll( skuMap );
        rabbitTemplate.convertAndSend( "","seckill_goods", JSON.toJSONString(orderMap) );
        System.out.println("5 ok!");
        return orderId; //返回订单号
    }

    private final String userKey="user_seckill_";

    /**
     * 返回该用户的某个商品的限购数量
     * @param username
     * @param skuId
     * @return
     */
    private int limitBuyNum(String username, String skuId) {
        //读取缓存中的秒杀商品
        Map map= (Map) redisTemplate.boundHashOps( listKey  ).get( skuId );
        if(map==null){
            return 0;
        }
        Integer limitNum= (Integer)map.get( "seckillLimit" );//获取该商品的限购数量
        //查询用户已经购买的数量
        Integer buyNum= (Integer) redisTemplate.boundHashOps( userKey + username ).get( skuId );
        if(buyNum==null){
            buyNum=0; ;  //如果用户没有购买过，则返回商品的限购数量
        }
        return limitNum-buyNum;//限购数量减去已购买数量
    }


    @Override
    public boolean deduction(Map orderMap) {
        String username = (String)orderMap.get( "username" );
        String skuId=(String)orderMap.get( "skuId" );
        int num=  Integer.parseInt( String.valueOf( orderMap.get( "num" ) )  )  ;

        //扣减秒杀商品缓存中的库存
        Map skuMap= (Map) redisTemplate.boundHashOps( listKey).get( skuId );
        Integer surplus= (Integer)skuMap.get( "seckillSurplus" ); //获取秒杀库存
        surplus=surplus-num;//减秒杀库存操作
        if(surplus<0){
            return false;
        }
        skuMap.put( "seckillSurplus", surplus);
        redisTemplate.boundHashOps( listKey  ).put( skuId, skuMap );//存回redis
        //记录用户购买数量
        Integer buyNum= (Integer) redisTemplate.boundHashOps( userKey + username ).get( skuId );
        if(buyNum==null){
            buyNum=0; ;  //如果用户没有购买过，则返回商品的限购数量
        }
        redisTemplate.boundHashOps( userKey + username ).put(skuId,buyNum+ num );  //该商品的累计购买数量记入缓存

        rabbitTemplate.convertAndSend( "","seckill_order",  JSON.toJSONString( orderMap ) ); //到下一站
        System.out.println("秒杀商品的秒杀库存扣减完成，发到秒杀订单队列");
        return true;
    }

}
