package com.changgou.goods.dao;

import com.changgou.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 根据分类名称查询品牌列表
     * @param categoryName
     * @return
     */
    @Select( "SELECT b.`name`,b.`image` " +
            "FROM tb_brand b ,tb_category c ,tb_category_brand cb " +
            "WHERE b.`id`=cb.`brand_id` AND c.`id`=cb.`category_id` AND c.name=#{categoryName}  " )
    public List<Map> findListByCategoryName(@Param( "categoryName" ) String categoryName);

}
