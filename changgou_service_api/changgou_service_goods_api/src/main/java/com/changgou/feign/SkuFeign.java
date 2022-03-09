package com.changgou.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient("goods")
public interface SkuFeign {

    /***
     * 多条件搜索sku
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/sku/search" )
    public Result findList(@RequestParam Map searchMap);


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/sku/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable("page") int page, @PathVariable("size")  int size);



    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/sku/{id}")
    public Result findById(@PathVariable("id") String id);


    /**
     * 扣减库存
     * @param map
     * @return
     */
    @PostMapping("/sku/deduction_stock")
    public Result deductionStock( @RequestBody Map map );

}
