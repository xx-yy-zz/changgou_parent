package com.changgou.business;

import com.changgou.business.service.AdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 微服务初始化类
 */
@Component
public class Init implements InitializingBean {

    @Autowired
    private AdService adService;

    @Override
    public void afterPropertiesSet() throws Exception {

        System.out.println("广告微服务初始化");
        String [] positionList={"web_index_lb"};  //所有广告位

        for(String position :positionList){
            adService.updateToRedis( position );
        }


    }
}
