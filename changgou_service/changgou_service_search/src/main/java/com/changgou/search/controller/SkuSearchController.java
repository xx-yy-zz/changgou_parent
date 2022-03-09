package com.changgou.search.controller;

import com.changgou.search.service.SkuSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sku_search")
public class SkuSearchController {

    @Autowired
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public Map search( @RequestParam Map<String,String> searchMap){
        return  skuSearchService.search( searchMap );
    }

}
