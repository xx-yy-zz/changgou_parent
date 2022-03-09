package com.changgou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.changgou.entity.Result;
import com.changgou.feign.CategoryFeign;
import com.changgou.feign.SpuFeign;
import com.changgou.page.service.PageService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private TemplateEngine templateEngine;

    @Value( "${page.path}" )
    private String path;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Value( "${domain.search}" )
    private String searchDomain;  //搜索域名

    @Value( "${domain.cart}" )
    private String cartDomain;// 购物车域名

    @Value( "${domain.seckill}" )
    private String seckillDomain;// 秒杀域名

    @Override
    public void createPage(String spuId) {

        Result goodsResult =  spuFeign.findById( spuId );  //根据spuid查询商品信息（spu+sku列表）
        Map goods = (Map)goodsResult.getData();
        Map spu = (Map)goods.get( "spu" );  //提取spu
        //商品分类
        List<String> categoryList = getCategoryList( spu );
        spu.put( "categoryList",categoryList );
        //spu图片处理
        String[] spuImages = ((String) spu.get( "images" )).split( "," );
        spu.put( "images",spuImages );
        //参数处理
        if(Strings.isEmpty( ( String ) spu.get( "paraItems" ))){
            spu.put( "paraItems", "{}");
        }
        Map paraItems = JSON.parseObject( (String) spu.get( "paraItems" ), Map.class );
        spu.put( "paraItems", paraItems);
        //规格选项列表处理
        if(Strings.isEmpty( ( String ) spu.get( "specItems" ))){
            spu.put( "specItems", "{}");
        }
        Map<String,List<String>> specItems = JSON.parseObject( (String) spu.get( "specItems" ), Map.class );
        spu.put( "specItems",specItems );

        List<Map> skuList= (List<Map>)goods.get( "skuList" );  //提取sku列表

        Map urlMap=new HashMap(  );
        for(Map sku:skuList){
            //spec规格
            if(Strings.isEmpty( ( String ) sku.get( "spec" ))){
                sku.put( "spec", "{}");
            }
            Map<String,String> spec = JSON.parseObject( (String) sku.get( "spec" ), Map.class );
            sku.put( "spec",spec );
            //1.创建urlmap   以{'颜色': '蓝色', '版本': '6GB+64GB'}作为key    以 100000773889.html作为值
            String specJson = JSON.toJSONString( spec, SerializerFeature.MapSortField );
            urlMap.put(  specJson,  (String)sku.get( "id" )+".html" );

            //2.规格选项大循环
            Map<String,String> skuSpec =new HashMap(  );
            for( String specKey : specItems.keySet()  ){
                for( String option: specItems.get( specKey ) ){
                    Map specMap = JSON.parseObject( specJson, Map.class );//当前sku
                    specMap.put( specKey,  option );//改变当前的sku
                    String s = JSON.toJSONString( specMap, SerializerFeature.MapSortField );
                    skuSpec.put( specKey+":"+ option ,  s);
                }
            }
            sku.put( "skuSpec",skuSpec );
        }
        spu.put( "urlMap",urlMap );


        for(Map sku:skuList ){

            //sku图片处理
            String[] skuImages = ((String) sku.get( "images" )).split( "," );
            sku.put( "images",skuImages );

            try {
                Context context=new Context();
                Map dataModel= new HashMap(  );
                dataModel.put( "spu",spu );
                dataModel.put( "sku",sku );
                dataModel.put( "searchDomain",searchDomain );
                dataModel.put( "cartDomain",cartDomain );
                dataModel.put( "seckillDomain",seckillDomain );
                context.setVariables( dataModel );
                File file=new File( path+"/"+(String)sku.get( "id" )+".html" );
                PrintWriter  writer = new PrintWriter( file,"utf-8" );
                templateEngine.process( "item",context,writer );
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


    }


    /**
     * 得到商品分类略表
     * @param spu
     * @return
     */
    private List<String> getCategoryList(Map spu){
        //商品分类
        List<String> categoryList=new ArrayList(  );
        Result category1Result = categoryFeign.findById( (Integer) spu.get( "category1Id" ) );
        Result category2Result = categoryFeign.findById( (Integer) spu.get( "category2Id" ) );
        Result category3Result = categoryFeign.findById( (Integer) spu.get( "category3Id" ) );

        Map category1=  (Map) category1Result.getData();
        Map category2=  (Map) category2Result.getData();
        Map category3=  (Map) category3Result.getData();
        String categoryName1= (String )category1.get( "name" );
        String categoryName2= (String )category2.get( "name" );
        String categoryName3= (String )category3.get( "name" );
        categoryList.add( categoryName1 );
        categoryList.add( categoryName2 );
        categoryList.add( categoryName3 );
        return categoryList;

    }

}
