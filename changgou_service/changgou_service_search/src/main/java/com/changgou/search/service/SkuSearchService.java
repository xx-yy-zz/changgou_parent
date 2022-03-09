package com.changgou.search.service;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务
 */
public interface SkuSearchService {


    void importSkuList(List<Map> skuList);

    /**
     * 根据spu id导入索引库
     * @param spuId
     */
    void importSkuListBySpuId(String spuId);

    /**
     * 导入全部数据
     */
    void importAll();

    /**
     * 搜索方法
     * @param searchMap
     * @return
     */
    Map search(Map<String,String> searchMap);

}
