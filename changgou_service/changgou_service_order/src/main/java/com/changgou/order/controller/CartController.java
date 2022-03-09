package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{username}")
    public Result findCart(@PathVariable String username){
        Map result = cartService.findCart( username );
        return new Result( true, StatusCode.OK,"查询成功",result );
    }

    /**
     * 增加商品到购物车
     * @param username
     * @param map
     * @return
     */
    @PostMapping("/{username}")
    public Result add(@PathVariable String username ,@RequestBody Map map ){
        cartService.add( username,(String)map.get( "skuId" ), (int)map.get( "num" ) );
        return new Result(  );
    }

    /**
     *  更改选中状态
     * @param username
     * @param map
     * @return
     */
    @PutMapping("/checked/{username}")
    public Result updateChecked(@PathVariable String username ,@RequestBody Map map ){
        cartService.updateChecked( username,(String)map.get( "skuId" ), (boolean)map.get( "checked" ) );
        return new Result(  );
    }


    /**
     * 删除选择的购物车
     * @param username
     * @return
     */
    @DeleteMapping("/checked/{username}")
    public Result deleteChecked(@PathVariable String username ){
        cartService.deleteChecked( username );
        return new Result(  );
    }

    /**
     * 选择的购物车移到收藏
     * @param username
     * @return
     */
    @PutMapping("/collect/{username}")
    public Result collectChecked(@PathVariable String username ){
        cartService.collectChecked( username );
        return new Result(  );
    }

    /**
     * 刷新购物车
     * @param username
     * @return
     */
    @GetMapping("/refresh/{username}")
    public Result refreshCartList(@PathVariable String username){
        cartService.refreshCartList( username );
        Map cart = cartService.findCart( username );
        return new Result( true, StatusCode.OK,"查询成功",cart);
    }

}
