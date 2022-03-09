package com.changgou.goods;

import com.changgou.goods.service.BrandService;
import com.changgou.goods.service.CategoryService;
import com.changgou.goods.service.SpecService;
import com.changgou.pojo.Category;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Init implements InitializingBean {


    @Autowired
    private RedisTemplate redisTemplate;

    private final String brandListKey="BRAND_LIST"; //品牌列表的key
    private final String specListKey="SPEC_LIST"; //规格列表的key


    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpecService specService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热");
        //查询分类列表  hash 大key  BRAND_LIST 小key  分类名称

        List<Category> categoryList = categoryService.findAll();

        for(Category category:categoryList ){
            //品牌列表
            List<Map> brandList = brandService.findListByCategoryName( category.getName() );
            redisTemplate.boundHashOps( brandListKey ).put(  category.getName(), brandList);
            //规格列表
            List<Map> specList = specService.findListByCategoryName( category.getName() );
            redisTemplate.boundHashOps( specListKey ).put( category.getName(), specList);
        }
        System.out.println("缓存预热完成");

    }
}
