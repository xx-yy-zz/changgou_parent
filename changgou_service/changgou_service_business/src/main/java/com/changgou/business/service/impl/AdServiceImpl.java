package com.changgou.business.service.impl;

import com.changgou.business.dao.AdMapper;
import com.changgou.business.service.AdService;
import com.changgou.pojo.Ad;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AdServiceImpl implements AdService {

    @Autowired
    private AdMapper adMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Ad> findAll() {
        return adMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Ad findById(Integer id){
        return  adMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param ad
     */
    @Override
    public void add(Ad ad){
        adMapper.insert(ad);
    }


    /**
     * 修改
     * @param ad
     */
    @Override
    public void update(Ad ad){
        adMapper.updateByPrimaryKey(ad);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Integer id){
        adMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Ad> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return adMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Ad> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Ad>)adMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Ad> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Ad>)adMapper.selectByExample(example);
    }

    @Value( "${ad_update_url}" )
    private String adUpdateUrl;

    @Override
    public void updateToRedis(String position) {

        OkHttpClient okHttpClient=new OkHttpClient();
        String url= adUpdateUrl+ "?position="+position;
        Request request =new Request.Builder().url( url ).build(); //构建请求对象
        Call call = okHttpClient.newCall( request );   //执行对象

        call.enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("执行错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("调用成功:"+response.message());
            }
        } );

    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 广告名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 广告位置
            if(searchMap.get("position")!=null && !"".equals(searchMap.get("position"))){
                criteria.andLike("position","%"+searchMap.get("position")+"%");
           	}
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
           	}
            // 图片地址
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // URL
            if(searchMap.get("url")!=null && !"".equals(searchMap.get("url"))){
                criteria.andLike("url","%"+searchMap.get("url")+"%");
           	}
            // 备注
            if(searchMap.get("remarks")!=null && !"".equals(searchMap.get("remarks"))){
                criteria.andLike("remarks","%"+searchMap.get("remarks")+"%");
           	}

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
