package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.feign.CartFeign;
import com.changgou.web.CasUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {


    @Autowired
    private CartFeign cartFeign;

    /**
     * 查询购物车列表
     * @param model
     * @return
     */
    @GetMapping
    public String list(Model model){
        Result result = cartFeign.findCart( CasUtil.loginName() );
        model.addAttribute( "result",result.getData() );
        return "cart";
    }

    /**
     * 购买
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/buy")
    public String buy(String skuId,int num){
        Map map=new HashMap();
        map.put( "skuId",skuId );
        map.put( "num",num );
        cartFeign.add( CasUtil.loginName(),map );
        return "redirect:/cart";
    }


    @GetMapping("/updateChecked")
    public String updateChecked(String skuId,boolean checked){
        Map map=new HashMap();
        map.put( "skuId",skuId );
        map.put( "checked",checked );
        cartFeign.updateChecked(  CasUtil.loginName(), map );
        return "redirect:/cart";
    }



    /**
     * 删除选中购物车
     * @return
     */
    @GetMapping("/deleteChecked")
    public String deleteChecked(){
        cartFeign.deleteChecked(  CasUtil.loginName() );
        return "redirect:/cart";
    }

    /**
     * 收藏选中购物车
     * @return
     */
    @GetMapping("/collectChecked")
    public String collectChecked(){
        cartFeign.collectChecked(  CasUtil.loginName() );
        return "redirect:/cart";
    }




}
