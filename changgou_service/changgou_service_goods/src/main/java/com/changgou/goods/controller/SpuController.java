package com.changgou.goods.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.service.SpuService;
import com.changgou.pojo.Goods;
import com.changgou.pojo.Spu;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Spu> spuList = spuService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",spuList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id){
        Goods goods = spuService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",goods);
    }


    /***
     * 新增数据
     * @param goods
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Goods goods){
        spuService.add(goods);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param goods
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Goods goods,@PathVariable String id){
        goods.getSpu().setId( id );
        spuService.update( goods );
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Spu> list = spuService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Spu> pageList = spuService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


    /**
     * 商品审核
     * @param spuId
     * @return
     */
    @PutMapping("/audit/{spuId}")
    public Result audit(@PathVariable String spuId){
        spuService.audit( spuId );
        return new Result(  );
    }



    /**
     * 商品下架
     * @param spuId
     * @return
     */
    @PutMapping("/pull/{spuId}")
    public Result pull(@PathVariable String spuId){
        spuService.pull( spuId );
        return new Result(  );
    }



    /**
     * 商品上架
     * @param spuId
     * @return
     */
    @PutMapping("/put/{spuId}")
    public Result put(@PathVariable String spuId){
        spuService.put( spuId );
        return new Result(  );
    }


    /**
     * 批量上架
     * @param ids
     * @return
     */
    @PutMapping("/putMany")
    public Result putMany(@RequestBody  String [] ids){
        int count = spuService.putMany( ids );
        return new Result( true,StatusCode.OK,"上架"+count+"个商品" );
    }

    /**
     * 还原
     * @param spuId
     * @return
     */
    @PutMapping("/restore/{spuId}")
    public Result restore(@PathVariable String spuId){
        spuService.restore( spuId );
        return new Result(  );
    }



    /**
     * 物理删除
     * @param id
     * @return
     */
    @DeleteMapping("/realDelete/{id}")
    public Result realDelete(@PathVariable String id){
        spuService.realDelete( id );
        return new Result(  );
    }

}
