package com.changgou.page.controller;

import com.changgou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/createPage")
    public void createPage(String spuId){
        pageService.createPage( spuId );
    }

}
