package com.changgou.goods.service.impl;

import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.service.SkuService;
import com.changgou.pojo.Sku;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Sku> findAll() {
        return skuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Sku findById(String id){
        return  skuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param sku
     */
    @Override
    public void add(Sku sku){
        skuMapper.insert(sku);
    }


    /**
     * 修改
     * @param sku
     */
    @Override
    public void update(Sku sku){
        skuMapper.updateByPrimaryKey(sku);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        skuMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Sku> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return skuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Sku> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Sku>)skuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Sku> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Sku>)skuMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public boolean deductionStock(Map<String, Integer> map) {

        //校验要扣减的库存是否都能扣减（）
        boolean flag=true;
        for(String skuId:map.keySet()){
            Sku sku = findById( skuId );
            if(sku==null){
                flag=false;
            }
            if(!"1".equals( sku.getStatus() )){
                flag=false;
            }
            if( map.get( skuId ).intValue()> sku.getNum()  ){
                flag=false;
            }
        }

        //执行批量扣减
        if(flag){
            for(String skuId:map.keySet()){
                skuMapper.deductionStock( skuId,map.get( skuId ) ); //扣减库存
                skuMapper.addSaleNum( skuId,map.get(skuId ) );//增加销量
            }
        }
        return flag;
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 商品id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
           	}
            // 商品条码
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
           	}
            // SKU名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 商品图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 商品图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // SPUID
            if(searchMap.get("spuId")!=null && !"".equals(searchMap.get("spuId"))){
                criteria.andEqualTo("spuId",searchMap.get("spuId"));
           	}
            // 类目名称
            if(searchMap.get("category_name")!=null && !"".equals(searchMap.get("category_name"))){
                criteria.andLike("category_name","%"+searchMap.get("category_name")+"%");
           	}
            // 品牌名称
            if(searchMap.get("brand_name")!=null && !"".equals(searchMap.get("brand_name"))){
                criteria.andLike("brand_name","%"+searchMap.get("brand_name")+"%");
           	}
            // 规格
            if(searchMap.get("spec")!=null && !"".equals(searchMap.get("spec"))){
                criteria.andLike("spec","%"+searchMap.get("spec")+"%");
           	}
            // 商品状态 1-正常，2-下架，3-删除
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
           	}

            // 价格（分）
            if(searchMap.get("price")!=null ){
                criteria.andEqualTo("price",searchMap.get("price"));
            }
            // 库存数量
            if(searchMap.get("num")!=null ){
                criteria.andEqualTo("num",searchMap.get("num"));
            }
            // 库存预警数量
            if(searchMap.get("alertNum")!=null ){
                criteria.andEqualTo("alertNum",searchMap.get("alertNum"));
            }
            // 重量（克）
            if(searchMap.get("weight")!=null ){
                criteria.andEqualTo("weight",searchMap.get("weight"));
            }
            // 类目ID
            if(searchMap.get("categoryId")!=null ){
                criteria.andEqualTo("categoryId",searchMap.get("categoryId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
