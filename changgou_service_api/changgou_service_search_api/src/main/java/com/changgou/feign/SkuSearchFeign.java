package com.changgou.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "search")
public interface SkuSearchFeign {

    @GetMapping("/sku_search/search")
    public Map search(@RequestParam Map<String,String> searchMap);

}
