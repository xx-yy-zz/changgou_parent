package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.*;
import com.changgou.goods.service.SpuService;
import com.changgou.pojo.*;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {

        long id = idWorker.nextId();
        System.out.println(id);
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Goods findById(String id){
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey( id );
        //查询sku列表
        Sku sku=new Sku();
        sku.setSpuId( id );
        List<Sku> skuList = skuMapper.select( sku );
        //封装为goods
        Goods goods=new Goods();
        goods.setSpu( spu );
        goods.setSkuList( skuList );
        return goods;
    }


    /**
     * 增加
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }


    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 删除（逻辑）
     * @param id
     */
    @Override
    public void delete(String id){
        Spu spu = spuMapper.selectByPrimaryKey( id );

        if("1".equals( spu.getIsMarketable() )){
            throw new RuntimeException( "上架商品不能删除" );
        }
        spu.setIsDelete( "1" );
        spu.setStatus( "0" );//修改为未审核
        spuMapper.updateByPrimaryKeySelective( spu );
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public void add(Goods goods) {
        //从组合实体类中提取spu 做保存
        Spu spu = goods.getSpu();
        spu.setId( idWorker.nextId()+"" );
        //spuMapper.insert( spu );  对象中包含null值  存储null值   insert spu (id,name) values (1,null);
        spuMapper.insertSelective( spu );  //对象中保存null值  忽略insert spu (id) values (1);
        //从组合实体类中提取sku列表 循环保存
        saveSkuList(goods);
        //添加分类与商品的关联
        addCategoryBrand( spu.getCategory3Id(),spu.getBrandId());
    }

    @Override
    public void update(Goods goods) {
        //保存spu
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKeySelective( spu );
        //删除原sku列表

        Example example=new Example( Sku.class );
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo( "spuId",spu.getId() );
        skuMapper.deleteByExample( example );
        //保存skulist
        saveSkuList(goods);
        //添加分类与商品的关联
        addCategoryBrand( spu.getCategory3Id(),spu.getBrandId());
    }

    @Override
    public void audit(String spuId) {
        Spu spu = spuMapper.selectByPrimaryKey( spuId );
        if(spu==null){
            throw new RuntimeException( "商品不存在" );
        }
        if("1".equals(  spu.getIsDelete()  )){
            throw new RuntimeException( "商品已经被删除" );
        }

        spu.setStatus( "1" );//已审核
        spu.setIsMarketable( "1" );//自动上架

        spuMapper.updateByPrimaryKeySelective(spu  );
    }

    @Override
    public void pull(String spuId) {
        Spu spu = new Spu();
        spu.setId( spuId );
        spu.setIsMarketable( "0" );//下架
        spuMapper.updateByPrimaryKeySelective( spu );
    }

    @Override
    public void put(String spuId) {
        Spu spu = spuMapper.selectByPrimaryKey( spuId );
        //校验
        if(spu==null){
            throw  new RuntimeException( "商品不存在 " );
        }
        if("1".equals(  spu.getIsDelete()  )){
            throw new RuntimeException( "商品已经被删除" );
        }
        if(!"1".equals(spu.getStatus()  )){
            throw new RuntimeException( "商品未审核" );
        }
        spu.setIsMarketable( "1" );//上架

        spuMapper.updateByPrimaryKeySelective(spu  );
    }

    @Override
    public int putMany(String[] ids) {
        Spu spu=new Spu();
        spu.setIsMarketable( "1" );

        Example example=new Example( Spu.class );
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn( "id", Arrays.asList( ids ) );
        criteria.andEqualTo( "isDelete","0" );//未删除
        criteria.andEqualTo( "status","1" );//已审核
        return spuMapper.updateByExampleSelective(spu,example);
    }

    @Override
    public void restore(String spuId) {
        Spu spu = spuMapper.selectByPrimaryKey( spuId );

        if("0".equals( spu.getIsDelete() )){
            throw new RuntimeException( "商品未删除" );
        }
        spu.setIsDelete( "0" );
        spuMapper.updateByPrimaryKeySelective( spu );

    }

    @Override
    @Transactional
    public void realDelete(String spuId) {
        //校验？
        Spu spu = spuMapper.selectByPrimaryKey( spuId );
        if("0".equals( spu.getIsDelete() )){
            throw new RuntimeException( "商品未逻辑删除" );
        }
        spuMapper.deleteByPrimaryKey( spuId );
        //删除sku列表
        Example example=new Example( Sku.class );
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo( "spuId",spuId );
        skuMapper.deleteByExample( example );
    }

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryMapper categoryMapper; //分类

    @Autowired
    private BrandMapper brandMapper; //品牌


    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 添加分类品牌关联表的数据
     * @param categoryId 分类id
     * @param brandId 品牌id
     */
    private void addCategoryBrand( int categoryId,int brandId ){
        CategoryBrand categoryBrand=new CategoryBrand();
        categoryBrand.setCategoryId( categoryId );
        categoryBrand.setBrandId( brandId );
        //查询关系记录是否存在
        int count = categoryBrandMapper.selectCount( categoryBrand );
        if(count==0){
            categoryBrandMapper.insert( categoryBrand );
        }
    }


    /**
     * 保存sku列表
     * @param goods
     */
    private void saveSkuList(Goods goods){
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();
        Date now = new Date();  //取得当前时间
        //分类查询
        Category category = categoryMapper.selectByPrimaryKey( spu.getCategory3Id() );
        //品牌
        Brand brand = brandMapper.selectByPrimaryKey( spu.getBrandId() );

        for(Sku sku:skuList ){
            if(sku.getId()==null){
                sku.setId(  idWorker.nextId()+""  );
            }
            sku.setSpuId( spu.getId() );
            //****  sku名称处理   spu名称+规格值组合   华为p30pro 红色 64G
            StringBuilder name = new StringBuilder( spu.getName() );
            //得到规格map
            Map<String,String> specMap = JSON.parseObject( sku.getSpec(), Map.class );
            for(String key: specMap.keySet()){
                name.append( " "+ specMap.get( key ) ) ;
            }
            sku.setName( name.toString() );
            //*******************************************************
            sku.setCreateTime( now  );
            sku.setUpdateTime( now );
            sku.setCategoryId( spu.getCategory3Id()  );//分类id
            sku.setCategoryName( category.getName() );//分类名称
            sku.setBrandName( brand.getName() );//品牌名称
            skuMapper.insertSelective( sku );
        }


    }


    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("sale_service")!=null && !"".equals(searchMap.get("sale_service"))){
                criteria.andLike("sale_service","%"+searchMap.get("sale_service")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("spec_items")!=null && !"".equals(searchMap.get("spec_items"))){
                criteria.andLike("spec_items","%"+searchMap.get("spec_items")+"%");
           	}
            // 参数列表
            if(searchMap.get("para_items")!=null && !"".equals(searchMap.get("para_items"))){
                criteria.andLike("para_items","%"+searchMap.get("para_items")+"%");
           	}
            // 是否上架
            if(searchMap.get("is_marketable")!=null && !"".equals(searchMap.get("is_marketable"))){
                criteria.andLike("is_marketable","%"+searchMap.get("is_marketable")+"%");
           	}
            // 是否启用规格
            if(searchMap.get("is_enable_spec")!=null && !"".equals(searchMap.get("is_enable_spec"))){
                criteria.andLike("is_enable_spec","%"+searchMap.get("is_enable_spec")+"%");
           	}
            // 是否删除
            if(searchMap.get("is_delete")!=null && !"".equals(searchMap.get("is_delete"))){
                criteria.andLike("is_delete","%"+searchMap.get("is_delete")+"%");
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
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
