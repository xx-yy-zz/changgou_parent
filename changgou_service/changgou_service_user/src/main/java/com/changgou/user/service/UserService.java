package com.changgou.user.service;

import com.changgou.pojo.User;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface UserService {

    /***
     * 查询所有
     * @return
     */
    List<User> findAll();

    /**
     * 根据ID查询
     * @param username
     * @return
     */
    User findById(String username);

    /***
     * 新增
     * @param user
     */
    void add(User user);

    /***
     * 修改
     * @param user
     */
    void update(User user);

    /***
     * 删除
     * @param id
     */
    void delete(String username);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<User> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<User> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<User> findPage(Map<String, Object> searchMap, int page, int size);


    /**
     * 发送短信验证码（发到mq和redis）
     * @param mobile
     */
    void sendSms(String mobile);

    /**
     * 用户注册
     * @param user
     * @param smsCode
     */
    void add(User user ,String smsCode);
}
