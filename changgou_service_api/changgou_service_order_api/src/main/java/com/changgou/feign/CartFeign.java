package com.changgou.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name="order")
public interface CartFeign {

    /**
     * 返回购物车列表
     * @param username
     * @return
     */
    @GetMapping("/cart/{username}")
    public Result findCart(@PathVariable("username") String username);

    /**
     * 添加商品到购物车
     * @param username
     */
    @PostMapping("/cart/{username}")
    public Result add(@PathVariable("username") String username, @RequestBody Map<String, Object> map);


    /**
     * 修改购物车的选中状态
     * @param username
     * @return
     */
    @PutMapping("/cart/checked/{username}")
    public Result updateChecked(@PathVariable("username") String username, @RequestBody Map<String, Object> map);

    /**
     * 删除选中的购物车
     * @param username
     * @return
     */
    @DeleteMapping("/cart/checked/{username}")
    public Result deleteChecked(@PathVariable("username") String username);


    /**
     * 收藏选中的购物车
     * @param username
     * @return
     */
    @PutMapping("/cart/collect/{username}")
    public Result collectChecked(@PathVariable("username") String username);

}