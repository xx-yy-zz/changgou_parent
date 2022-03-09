package com.changgou.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "goods")
public interface CategoryFeign {

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/category/{id}")
    public Result findById(@PathVariable("id") Integer id);

}
