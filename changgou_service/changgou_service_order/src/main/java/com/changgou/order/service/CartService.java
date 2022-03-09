package com.changgou.order.service;

import java.util.List;
import java.util.Map;

/**
 * 购物车业务接口
 */
public interface CartService {


    /**
     * 获取某用户的购物车
     * @param username
     * @return
     */
    Map findCart(String username);

    /**
     * 购物车新增商品或数量
     * @param username
     * @param skuId
     * @param num
     */
    void add(String username,String skuId,int num);

    /**
     * 更改勾选状态
     * @param username
     * @param skuId
     * @param checked
     */
    void updateChecked(String username,String skuId,boolean checked);

    /**
     * 删除选中的购物车
     * @param username
     */
    void deleteChecked(String username);

    /**
     * 收藏选中的购物车
     * @param username
     */
    void collectChecked(String username);


    /**
     * 刷新购物车
     * @param username
     * @return
     */
    List<Map> refreshCartList(String username);

}
