package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.changgou.entity.Result;
import com.changgou.feign.SkuFeign;
import com.changgou.feign.WxPayFeign;
import com.changgou.order.dao.OrderConfigMapper;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderLogMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.pojo.Order;
import com.changgou.pojo.OrderConfig;
import com.changgou.pojo.OrderItem;
import com.changgou.pojo.OrderLog;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey( id );
    }


    @Autowired
    private CartService cartService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 增加
     *
     * @param order
     */
    @Override
    @GlobalTransactional(name = "order_add")
    public Order add(Order order) {

        //获取购物车，并校验
        cartService.refreshCartList( order.getUsername() );//刷新购物车
        Map cart = cartService.findCart( order.getUsername() );
        List<Map> cartList = (List<Map>) cart.get( "cartList" );  //提取购物车
        Integer payMoney = (Integer) cart.get( "payMoney" ); //支付金额

        System.out.println( "payMoney:" + payMoney );
        if (payMoney.intValue() <= 0) {
            throw new RuntimeException( "没有要支付的订单" );
        }

        //扣减库存（）
        //封装map
        Map<String, Integer> stockMap = new HashMap();
        for (Map map : cartList) {
            if ((boolean) map.get( "checked" )) {
                OrderItem orderItem = (OrderItem) map.get( "item" );
                stockMap.put( orderItem.getSkuId(), orderItem.getNum() );
            }
        }
        Result deductionResult = skuFeign.deductionStock( stockMap );
        if (!deductionResult.isFlag()) {
            throw new RuntimeException( "扣减库存失败！" );
        }

        //保存订单主表
        String orderId = idWorker.nextId() + "";
        order.setId( orderId );
        order.setCreateTime( new Date() );//创建日期
        order.setTotalNum( (Integer) cart.get( "num" ) );
        order.setTotalMoney( (Integer) cart.get( "money" ) );
        order.setPreMoney( (Integer) cart.get( "preMoney" ) );
        order.setPayMoney( payMoney );

        orderMapper.insertSelective( order );

        //保存订单明细表
        for (Map map : cartList) {
            if ((boolean) map.get( "checked" )) {
                OrderItem orderItem = (OrderItem) map.get( "item" );
                orderItem.setId( idWorker.nextId() + "" );
                orderItem.setOrderId( orderId );
                orderItemMapper.insertSelective( orderItem );
            }
        }

        //删除选中的购物车
        cartService.deleteChecked( order.getUsername() );

        //发送订单号到消息
        rabbitTemplate.convertAndSend( "","queue.ordercreate",orderId );

        return order;

    }


    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey( order );
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey( id );
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample( searchMap );
        return orderMapper.selectByExample( example );
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage( page, size );
        return (Page<Order>) orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage( page, size );
        Example example = createExample( searchMap );
        return (Page<Order>) orderMapper.selectByExample( example );
    }

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Override
    @Transactional
    public void updatePayStatus(String orderId, String transactionId) {
        Order order = orderMapper.selectByPrimaryKey( orderId );
        if (order != null && "0".equals( order.getPayStatus() )) {
            //更新订单表状态
            order.setPayStatus( "1" ); //支付状态  0 未支付   1已支付
            order.setOrderStatus( "1" ); //订单状态  0.未支付 1 未发货
            order.setPayTime( new Date() );//支付日期
            order.setUpdateTime( new Date() );//更新日期
            order.setTransactionId( transactionId );  //微信交易流水号
            orderMapper.updateByPrimaryKeySelective( order );//更新

            //记录订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId( idWorker.nextId() + "" );
            orderLog.setOrderStatus( "1" );
            orderLog.setPayStatus( "1" );
            orderLog.setOperater( "system" );
            orderLog.setOperateTime( new Date() );
            orderLog.setRemarks( "微信支付流水号：" + transactionId );

            orderLogMapper.insertSelective( orderLog );

        }
    }

    @Autowired
    private WxPayFeign wxPayFeign;


    @Override
    @GlobalTransactional(name = "closeOrder")
    public void closeOrder(String orderId) {
        System.out.println("开始关闭订单");
        //根据orderId查询本地订单
        Order order = orderMapper.selectByPrimaryKey( orderId );
        //做非空校验  做状态校验
        if (order == null) {
            throw new RuntimeException( "订单不存在" );
        }
        if (!"0".equals( order.getOrderStatus() )) {
            System.out.println( "此订单不用关闭" );
            return;
        }
        //查询微信订单
        System.out.println("通过校验");
        Result wxPayResult = wxPayFeign.queryOrder( orderId );
        Map<String,String> wxPayMap= (Map)wxPayResult.getData();
        System.out.println("微信查询结果："+ wxPayMap );

        //如果微信订单已支付 ，做状态补偿
        if("SUCCESS".equals(  wxPayMap.get( "trade_state" ) )){
            updatePayStatus(orderId, wxPayMap.get( "transaction_id" )  );
            System.out.println("微信订单已支付 ，做状态补偿");
        }

        //如果微信支付未支付，做关闭订单处理
        if("NOTPAY".equals( wxPayMap.get( "trade_state" ) )){
            System.out.println("执行关闭订单操作");
            //----关闭订单处理-----
            //1.修改订单表状态
            order.setOrderStatus( "4" );//订单表状态  0：未支付  1：未发货（已支付） 2：已发货  3：已结束  4：已关闭
            order.setCloseTime( new Date(  ) );//记录关闭时间
            order.setUpdateTime( new Date(  ) );//更新时间（状态发生变化，就会记录此时间）
            orderMapper.updateByPrimaryKeySelective( order );

            //2.在订单日志中添加记录  (记录订单变化轨迹)
            OrderLog orderLog = new OrderLog();
            orderLog.setId( idWorker.nextId() + "" );
            orderLog.setOrderStatus( "4" );
            orderLog.setOperater( "system" );//system 系统  admin 管理员  user 用户
            orderLog.setOperateTime( new Date() );
            orderLog.setRemarks( "系统关单" );
            orderLogMapper.insertSelective( orderLog );

            //3.回退库存和销量数据

            Map<String,Integer> stockMap=new HashMap(  );  //key skuId  value:数量
            OrderItem orderItem=new OrderItem();
            orderItem.setOrderId(  orderId );
            List<OrderItem> orderItemList = orderItemMapper.select( orderItem );//订单明细表记录
            for(OrderItem orderItem1:orderItemList){
                stockMap.put( orderItem1.getSkuId(),  -orderItem1.getNum()   ); //注意数量要为负数（负扣减）
            }
            skuFeign.deductionStock( stockMap ); //回退库存与销量数据

            //4.关闭微信订单
            wxPayFeign.closeOrder( orderId );

        }


    }

    @Override
    @Transactional
    public void batchSend(List<Order> orderList) {

        //数据非空校验
        for(Order order:orderList){
            if( order.getId()==null  || "".equals( order.getId() ) ){
                throw  new RuntimeException( "订单id不能为空" );
            }
            if( order.getShippingName()==null || "".equals( order.getShippingName() ) ){
                throw  new RuntimeException( "物流公司不能为空" );
            }
            if( order.getShippingCode()==null  || "".equals( order.getShippingCode() ) ){
                throw  new RuntimeException( "物流单号不能为空" );
            }
        }

        //订单状态校验
        for(Order order:orderList){
            Order order1 = orderMapper.selectByPrimaryKey( order.getId() );
            if(order1==null){
                throw  new RuntimeException( "订单不存在" );
            }
            if(!"1".equals( order1.getOrderStatus() )) {
                throw  new RuntimeException( "订单状态错误" );
            }
            if(!"0".equals( order1.getConsignStatus() )) {
                throw  new RuntimeException( "订单发货状态错误" );
            }
        }

        //执行修改

        for(Order order:orderList){

            //修改订单状态
            order.setOrderStatus( "2" );
            order.setConsignStatus( "1" );
            order.setConsignTime( new Date(  ) );
            order.setUpdateTime( new Date(  ) );
            orderMapper.updateByPrimaryKeySelective( order );

            //记录日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId( idWorker.nextId() + "" );
            orderLog.setOrderStatus( "2" );
            orderLog.setConsignStatus( "1" );
            orderLog.setOperater( "admin" );//system 系统  admin 管理员  user 用户
            orderLog.setOperateTime( new Date() );
            orderLog.setRemarks( "批量发货" );
            orderLogMapper.insertSelective( orderLog );

        }
    }

    @Override
    @Transactional
    public void tack(String orderId, String operator) {

        //1. 校验
        Order order = orderMapper.selectByPrimaryKey( orderId );
        if(order==null){
            throw  new RuntimeException( "订单不存在" );
        }
        //确认收货的订单一定是已发货的订单
        if(!"2".equals( order.getOrderStatus() ) || !"1".equals( order.getConsignStatus() ) ){
            throw  new RuntimeException( "订单状态错误" );
        }

        //2.修改订单状态
        order.setOrderStatus( "3" );
        order.setEndTime( new Date(  ) );
        order.setUpdateTime( new Date(  ) );
        orderMapper.updateByPrimaryKeySelective( order );

        //3.保存订单日志
        OrderLog orderLog = new OrderLog();
        orderLog.setId( idWorker.nextId() + "" );
        orderLog.setOrderStatus( "3" );
        orderLog.setOperater( operator );//system 系统  admin 管理员  user 用户
        orderLog.setOperateTime( new Date() );
        orderLog.setRemarks( "确认收货" );
        orderLogMapper.insertSelective( orderLog );

    }

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Override
    public void autoTack() {

        //1.从订单配置表中取出订单自动确认的期限
        OrderConfig orderConfig =   orderConfigMapper.selectByPrimaryKey( 1 );
        int takeTimeoutDays = orderConfig.getTakeTimeout().intValue();  //得到期限
        //2.得到当前日期开始向前数（订单自动确认的期限）天  过期时间 2019-7-4

        LocalDate now =LocalDate.now();//获取当前时间
        LocalDate date = now.plusDays( -takeTimeoutDays );  //获取过期节点

        //3.从订单表中查询过期订单（发货日期小于过期时间 并且状态是未确认收货状态 ）
        Example example=new Example( Order.class );
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan( "consignTime",date );
        criteria.andEqualTo( "orderStatus","2" );
        List<Order> orderList = orderMapper.selectByExample( example );
        //4.循环批量处理

        for(Order order:orderList){
            System.out.println("处理未确认订单："+ order.getId());
            tack( order.getId(),"system" );
        }
        System.out.println("处理未确认订单完毕");

    }



    @Override
    @Transactional
    public Order addSeckill(Map map) {
        int price= (Integer) map.get( "seckillPrice" ) ;//秒杀价格
        int skuPrice=  (Integer) map.get( "skuPrice" )  ; //sku价格
        int num= (Integer) map.get( "num" ) ;//购买数量
        String orderId= (String)map.get( "orderId" );//订单编号
        String skuId =(String)map.get( "skuId" );//sku id
        //创建订单主表记录
        Order order=new Order();
        order.setId(orderId);
        order.setCreateTime( new Date(  ) );
        order.setTotalMoney(  skuPrice* num );
        order.setTotalNum( num );
        order.setPayMoney( price* num );
        order.setPreMoney( order.getTotalMoney()-order.getPayMoney() );//优惠金额为差价
        order.setUsername( (String)map.get( "username" ));
        orderMapper.insertSelective( order );
        //创建订单明细表记录
        OrderItem orderItem=new OrderItem();
        orderItem.setId( orderId );
        orderItem.setOrderId( orderId );
        orderItem.setName( (String)map.get( "skuName" ) );
        orderItem.setSkuId( skuId );
        orderItem.setPrice( price );//成交价格
        orderItem.setMoney( skuPrice* num ); //原金额
        orderItem.setPayMoney(price*num  );//支付金额
        orderItemMapper.insertSelective(orderItem  );

        /*
        try {
            Thread.sleep( 15000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //发送给秒杀支付队列
        rabbitTemplate.convertAndSend( "","seckill_pay", JSON.toJSONString(order) );
        //发送到库存扣减队列
        //.....(略)
        rabbitTemplate.convertAndSend( "","queue.seckillorder_create" ,orderId);

        System.out.println("秒杀订单已经创建成功，发送到秒杀支付队列");
        return order;
    }


    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example( Order.class );
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get( "id" ) != null && !"".equals( searchMap.get( "id" ) )) {
                criteria.andLike( "id", "%" + searchMap.get( "id" ) + "%" );
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get( "pay_type" ) != null && !"".equals( searchMap.get( "pay_type" ) )) {
                criteria.andLike( "pay_type", "%" + searchMap.get( "pay_type" ) + "%" );
            }
            // 物流名称
            if (searchMap.get( "shipping_name" ) != null && !"".equals( searchMap.get( "shipping_name" ) )) {
                criteria.andLike( "shipping_name", "%" + searchMap.get( "shipping_name" ) + "%" );
            }
            // 物流单号
            if (searchMap.get( "shipping_code" ) != null && !"".equals( searchMap.get( "shipping_code" ) )) {
                criteria.andLike( "shipping_code", "%" + searchMap.get( "shipping_code" ) + "%" );
            }
            // 用户名称
            if (searchMap.get( "username" ) != null && !"".equals( searchMap.get( "username" ) )) {
                criteria.andLike( "username", "%" + searchMap.get( "username" ) + "%" );
            }
            // 买家留言
            if (searchMap.get( "buyer_message" ) != null && !"".equals( searchMap.get( "buyer_message" ) )) {
                criteria.andLike( "buyer_message", "%" + searchMap.get( "buyer_message" ) + "%" );
            }
            // 是否评价
            if (searchMap.get( "buyer_rate" ) != null && !"".equals( searchMap.get( "buyer_rate" ) )) {
                criteria.andLike( "buyer_rate", "%" + searchMap.get( "buyer_rate" ) + "%" );
            }
            // 收货人
            if (searchMap.get( "receiver_contact" ) != null && !"".equals( searchMap.get( "receiver_contact" ) )) {
                criteria.andLike( "receiver_contact", "%" + searchMap.get( "receiver_contact" ) + "%" );
            }
            // 收货人手机
            if (searchMap.get( "receiver_mobile" ) != null && !"".equals( searchMap.get( "receiver_mobile" ) )) {
                criteria.andLike( "receiver_mobile", "%" + searchMap.get( "receiver_mobile" ) + "%" );
            }
            // 收货人地址
            if (searchMap.get( "receiver_address" ) != null && !"".equals( searchMap.get( "receiver_address" ) )) {
                criteria.andLike( "receiver_address", "%" + searchMap.get( "receiver_address" ) + "%" );
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get( "source_type" ) != null && !"".equals( searchMap.get( "source_type" ) )) {
                criteria.andLike( "source_type", "%" + searchMap.get( "source_type" ) + "%" );
            }
            // 交易流水号
            if (searchMap.get( "transaction_id" ) != null && !"".equals( searchMap.get( "transaction_id" ) )) {
                criteria.andLike( "transaction_id", "%" + searchMap.get( "transaction_id" ) + "%" );
            }
            // 订单状态
            if (searchMap.get( "order_status" ) != null && !"".equals( searchMap.get( "order_status" ) )) {
                criteria.andLike( "order_status", "%" + searchMap.get( "order_status" ) + "%" );
            }
            // 支付状态
            if (searchMap.get( "pay_status" ) != null && !"".equals( searchMap.get( "pay_status" ) )) {
                criteria.andLike( "pay_status", "%" + searchMap.get( "pay_status" ) + "%" );
            }
            // 发货状态
            if (searchMap.get( "consign_status" ) != null && !"".equals( searchMap.get( "consign_status" ) )) {
                criteria.andLike( "consign_status", "%" + searchMap.get( "consign_status" ) + "%" );
            }
            // 是否删除
            if (searchMap.get( "is_delete" ) != null && !"".equals( searchMap.get( "is_delete" ) )) {
                criteria.andLike( "is_delete", "%" + searchMap.get( "is_delete" ) + "%" );
            }

            // 数量合计
            if (searchMap.get( "totalNum" ) != null) {
                criteria.andEqualTo( "totalNum", searchMap.get( "totalNum" ) );
            }
            // 金额合计
            if (searchMap.get( "totalMoney" ) != null) {
                criteria.andEqualTo( "totalMoney", searchMap.get( "totalMoney" ) );
            }
            // 优惠金额
            if (searchMap.get( "preMoney" ) != null) {
                criteria.andEqualTo( "preMoney", searchMap.get( "preMoney" ) );
            }
            // 邮费
            if (searchMap.get( "postFee" ) != null) {
                criteria.andEqualTo( "postFee", searchMap.get( "postFee" ) );
            }
            // 实付金额
            if (searchMap.get( "payMoney" ) != null) {
                criteria.andEqualTo( "payMoney", searchMap.get( "payMoney" ) );
            }

        }
        return example;
    }

}
