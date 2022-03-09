package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.feign.SkuFeign;
import com.changgou.search.service.SkuSearchService;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void importSkuList(List<Map> skuList) {

        //1.创建大容量请求对象（批处理）
        BulkRequest bulkRequest=new BulkRequest();

        //2.封装大容量请求对象

        for(Map sku:skuList){
            IndexRequest indexRequest=new IndexRequest( "sku","doc",  String.valueOf(sku.get( "id" ) )  );
            Map spec =  JSON.parseObject( String.valueOf(sku.get( "spec" )  ),Map.class ) ;//规格由json字符串转换为对象
            sku.put( "spec",spec );
            indexRequest.source( sku );
            bulkRequest.add( indexRequest );
        }

        //3.执行大容量请求

        restHighLevelClient.bulkAsync( bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                System.out.println("导入成功："+bulkItemResponses.status());
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("导入错误");
                e.printStackTrace();
            }
        } );

    }

    @Autowired
    private SkuFeign skuFeign;

    @Override
    public void importSkuListBySpuId(String spuId) {

        //查询sku列表
        Map map=new HashMap(  );
        map.put( "spuId",spuId );
        map.put( "status","1" );

        Result skuResult = skuFeign.findList( map );
        //导入数据
        if( skuResult.isFlag() ){  //如果查询成功
            System.out.println("获取数据成功，执行导入");
            List<Map> skuList= (List<Map>)skuResult.getData();
            importSkuList( skuList );
        }else {
            System.out.println("获取数据失败");
        }

    }

    @Override
    public void importAll() {
        System.out.println("导入全部索引数据");
        Map map=new HashMap(  );
        map.put( "status","1" );

        int page=1;
        while(true){
            Result result = skuFeign.findPage( map, page, 1000 );
            List<Map> skuList= (List<Map>)  ((Map)result.getData()).get( "rows" );   //{total:100,rows:[]}
            if(skuList.size()==0){
                break;
            }
            System.out.println("页码："+page);
            importSkuList(skuList);
            page++;
        }
        System.out.println("导入全部索引数据--结束");

    }


    @Autowired
    private RedisTemplate redisTemplate;

    private final String brandListKey="BRAND_LIST"; //品牌列表的key
    private final String specListKey="SPEC_LIST"; //规格列表的key


    /**
     * 搜索逻辑
     * @param searchMap
     * keywords  关键字
     * category  分类名称
     * brand  品牌
     * spec.规格名称.keyword  规格
     * price  0-500   3000-*  * 代表无穷大
     * pageNo 页码
     * sort 排序字段
     * sortOrder 升序或降序  desc  asc
     * @return
     * rows  商品列表
     * categoryList  分类列表
     * brandList  品牌列表
     * specList  规格列表
     */
    @Override
    public Map search(Map<String, String> searchMap) {

        //1.封装请求对象

        SearchRequest searchRequest=new SearchRequest( "sku" );
        searchRequest.types( "doc" );

        //1.1 查询条件
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder(  );
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //1.1.1 关键字查询
        if(searchMap.get( "keywords" )!=null){
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery( "name", searchMap.get( "keywords" ) );
            boolQueryBuilder.must(matchQueryBuilder  );
        }


        //1.1.2 分类筛选
        if(searchMap.get( "category" )!=null){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery( "categoryName", searchMap.get( "category" ) );
            boolQueryBuilder.filter( termQueryBuilder );
        }

        //1.1.3 品牌筛选
        if(searchMap.get( "brand" )!=null){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery( "brandName", searchMap.get( "brand" ) );
            boolQueryBuilder.filter( termQueryBuilder );
        }

        //1.1.4 规格筛选
        for( String key: searchMap.keySet()){
            if(key.startsWith( "spec." )){ //规格
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery( key+".keyword", searchMap.get( key ) );
                boolQueryBuilder.filter( termQueryBuilder );
            }
        }

        //1.1.5 价格筛选
        if( searchMap.get( "price" )!=null ){
            String[] price = searchMap.get( "price" ).split( "-" );  //price[0]  price[1]
            if(!price[0].equals( "0" )){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery( "price" ).gte( price[0] + "00" );
                boolQueryBuilder.filter( rangeQueryBuilder );
            }
            if(!price[1].equals( "*" )){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery( "price" ).lte( price[1] + "00" );
                boolQueryBuilder.filter( rangeQueryBuilder );
            }
        }

        searchSourceBuilder.query( boolQueryBuilder );


        //1.2 高亮
        HighlightBuilder highlightBuilder=new HighlightBuilder(  );
        highlightBuilder.field( "name" ).preTags( "<font style='color:red'>" ).postTags( "</font>" );
        searchSourceBuilder.highlighter( highlightBuilder );

        //1.3 聚合（分组）
        TermsAggregationBuilder termsAggregationBuilder= AggregationBuilders.terms( "sku_category" ).field( "categoryName" );
        searchSourceBuilder.aggregation( termsAggregationBuilder );

        //1.4 分页
        if(searchMap.get( "pageNo" )==null){  //容错处理
            searchMap.put( "pageNo","1" );
        }
        int size=30; //页大小
        int fromIndex  =  (Integer.parseInt( searchMap.get( "pageNo" ) ) -1)*size;
        searchSourceBuilder.from( fromIndex );
        searchSourceBuilder.size( size );


        //1.5 排序
        if(searchMap.get( "sort" )==null){
            searchMap.put( "sort","" );
        }
        if(searchMap.get( "sortOrder" )==null){
            searchMap.put( "sortOrder","DESC" ); //默认降序
        }
        if(!"".equals( searchMap.get( "sort" ) )){
            searchSourceBuilder.sort( searchMap.get( "sort" ), SortOrder.valueOf( searchMap.get( "sortOrder" ) ) );
        }


        searchRequest.source( searchSourceBuilder );//最外边的大括号
        //2.封装查询结果
        Map map=new HashMap(  );
        try {
            SearchResponse searchResponse = restHighLevelClient.search( searchRequest, RequestOptions.DEFAULT );//得到查询响应对象
            SearchHits searchHits =  searchResponse.getHits();

            //2.1 商品列表
            List<Map> skuList=new ArrayList<>(  );//商品列表
            SearchHit[] hits = searchHits.getHits();
            for(SearchHit searchHit:hits){
                Map<String, Object> source = searchHit.getSourceAsMap();
                if(searchMap.get( "keywords" )!=null){
                    //2.1.1 高亮处理
                    Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                    HighlightField name = highlightFields.get( "name" );
                    Text[] fragments = name.fragments();
                    source.put( "name",  fragments[0].toString() ) ;//用高亮内容替换原内容
                }
                //..............................
                skuList.add( source );
            }
            map.put( "rows",skuList );

            //2.2 分组列表
            Aggregations aggregations =  searchResponse.getAggregations();
            Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
            Terms terms = (Terms)aggregationMap.get( "sku_category" );
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            List<String> categoryList=new ArrayList<>(  );//分类名称集合
            for(Terms.Bucket bucket:buckets){
                categoryList.add( bucket.getKeyAsString() );
            }
            map.put( "categoryList", categoryList);

            //2.3 品牌与规格列表查询

            String categoryName = "";
            if(categoryList.size()>0){
                categoryName= categoryList.get( 0 );
            }
            //2.3.1 品牌列表
            List<Map> brandList = (List<Map>)redisTemplate.boundHashOps( brandListKey ).get( categoryName );
            if(brandList!=null){
                map.put( "brandList",brandList );
            }else{
                map.put( "brandList",new ArrayList<>(  ) );
            }


            //2.3.2 规格列表
            List<Map> specList = (List<Map>)redisTemplate.boundHashOps( specListKey ).get( categoryName );
            if(specList!=null){
                map.put( "specList",specList );
            }else{
                map.put( "specList",new ArrayList<>(  ) );
            }

            //2.4 返回页码
            long totalCount = searchHits.getTotalHits();  //总记录数
            long totalPages = ( totalCount%size==0 )?(totalCount/size):(totalCount/size+1)  ;  //页码数
            map.put( "totalPages", totalPages);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
