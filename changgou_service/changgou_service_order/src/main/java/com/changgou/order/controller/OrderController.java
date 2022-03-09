package com.changgou.order.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.service.OrderService;
import com.changgou.pojo.Order;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/order")
public class OrderController {


    @Autowired
    private OrderService orderService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Order> orderList = orderService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",orderList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id){
        Order order = orderService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",order);
    }


    /***
     * 新增数据
     * @param order
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Order order){
        order = orderService.add( order );
        return new Result(true,StatusCode.OK,"添加成功",order);
    }


    /***
     * 修改数据
     * @param order
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Order order,@PathVariable String id){
        order.setId(id);
        orderService.update(order);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        orderService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Order> list = orderService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Order> pageList = orderService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


    /**
     * 批量发货
     * @param orderList
     * @return
     */
    @PostMapping("/batchSend")
    public Result batchSend(@RequestBody List<Order> orderList){
        orderService.batchSend( orderList );
        return new Result( true,StatusCode.OK,"批量发货成功" );
    }


    /**
     * 确认收货
     * @param orderId
     * @param operator
     * @return
     */
    @PutMapping("/tack/{orderId}/operator/{operator}")
    public Result tack(@PathVariable String orderId,@PathVariable String operator){
        orderService.tack( orderId,operator );
        return new Result( true,StatusCode.OK,"确认收货成功" );
    }

    /**
     * 自动确认收货
     * @return
     */
    @GetMapping("/autoTack")
    public Result autoTack(){
        orderService.autoTack();
        return new Result(  );
    }

}
