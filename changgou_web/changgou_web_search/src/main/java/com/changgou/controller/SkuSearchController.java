package com.changgou.controller;

import com.changgou.feign.SkuSearchFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SkuSearchController {

    @Autowired
    private SkuSearchFeign skuSearchFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping("/search")
    public String search(Model model, @RequestParam Map<String, String> searchMap){
        //参数容错
        if(searchMap.get( "pageNo" )==null){
            searchMap.put( "pageNo","1" );
        }

        if(searchMap.get( "sort" )==null){
            searchMap.put( "sort","" );
        }
        if(searchMap.get( "sortOrder" )==null){
            searchMap.put( "sortOrder","DESC" );
        }

        Map result = skuSearchFeign.search( searchMap );

        //url拼接
        StringBuffer url=new StringBuffer( "/search?" );
        for(String key:searchMap.keySet()){
            url.append( "&"+key+"="+searchMap.get( key ) );
        }
        model.addAttribute( "url",url.toString() );

        model.addAttribute( "result",result );
        model.addAttribute( "searchMap",searchMap );


        //页码处理
        Integer pageNo =  Integer.parseInt(  searchMap.get( "pageNo" )) ;//当前页码
        Integer totalPages=(Integer)result.get( "totalPages" ); //总页数
        Integer startPage=1;  //开始页码
        Integer endPage= totalPages ; //截至页码
        if(totalPages>5){  //以当前页码为中心的五个页码
            startPage=pageNo-2;
            if(startPage<1){
                startPage=1;
            }
            endPage=startPage+4;

            if(endPage>totalPages){
                endPage=totalPages;
                startPage=endPage-4;
            }

        }

        model.addAttribute( "startPage",startPage );
        model.addAttribute( "endPage",endPage );
        model.addAttribute( "totalPages",totalPages );//总页数
        model.addAttribute( "pageNo" ,pageNo);


        return "search";
    }


}
