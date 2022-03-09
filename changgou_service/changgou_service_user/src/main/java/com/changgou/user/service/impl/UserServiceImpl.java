package com.changgou.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.user.dao.UserMapper;
import com.changgou.user.service.UserService;
import com.changgou.pojo.User;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param username
     * @return
     */
    @Override
    public User findById(String username){
        return  userMapper.selectByPrimaryKey(username);
    }


    /**
     * 增加
     * @param user
     */
    @Override
    public void add(User user){
        userMapper.insert(user);
    }


    /**
     * 修改
     * @param user
     */
    @Override
    public void update(User user){
        userMapper.updateByPrimaryKey(user);
    }

    /**
     * 删除
     * @param username
     */
    @Override
    public void delete(String username){
        userMapper.deleteByPrimaryKey(username);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<User> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return userMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<User> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<User>)userMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<User> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<User>)userMapper.selectByExample(example);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendSms(String mobile) {

        //1.生成短信验证码（6位）
        Random random=new Random();
        int max=999999;//最大数
        int min=100000;//最小值
        int code = random.nextInt( max );
        if( code<min ){
            code=code+min;
        }
        System.out.println("短信验证码："+code);
        //2.存到redis
        redisTemplate.boundValueOps( "code_"+mobile ).set( code+"",5, TimeUnit.MINUTES );
        Map map=new HashMap(  );
        map.put( "mobile",mobile );
        map.put( "code",code+"" );
        //3.存到rabbitmq中
        rabbitTemplate.convertAndSend( "","sms", JSON.toJSONString(map) );

    }

    @Override
    public void add(User user, String smsCode) {

        //从redis中提取短信验证码
        String sysCode= (String)redisTemplate.boundValueOps( "code_"+user.getPhone() ).get();
        if(sysCode==null){
            throw new RuntimeException( "请点击发送短信验证码" );
        }
        if(!sysCode.equals( smsCode )){
            throw new RuntimeException( "短信验证码填写错误" );
        }

        //校验用户是否重复
        User searchUser=new User();
        searchUser.setUsername( user.getPhone()  );
        int count = userMapper.selectCount( searchUser );
        if(count>0){
            throw new RuntimeException( "手机号已经被注册" );
        }

        user.setUsername( user.getPhone() );//将手机号做为用户名
        user.setCreated( new Date(  ) );
        user.setUpdated( new Date(  ) );
        //密码加密
        String password = BCrypt.hashpw( user.getPassword(), BCrypt.gensalt() );
        user.setPassword( password );
        userMapper.insertSelective( user );
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
           	}
            // 密码，加密存储
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andLike("password","%"+searchMap.get("password")+"%");
           	}
            // 注册手机号
            if(searchMap.get("phone")!=null && !"".equals(searchMap.get("phone"))){
                criteria.andLike("phone","%"+searchMap.get("phone")+"%");
           	}
            // 注册邮箱
            if(searchMap.get("email")!=null && !"".equals(searchMap.get("email"))){
                criteria.andLike("email","%"+searchMap.get("email")+"%");
           	}
            // 会员来源：1:PC，2：H5，3：Android，4：IOS
            if(searchMap.get("source_type")!=null && !"".equals(searchMap.get("source_type"))){
                criteria.andLike("source_type","%"+searchMap.get("source_type")+"%");
           	}
            // 昵称
            if(searchMap.get("nick_name")!=null && !"".equals(searchMap.get("nick_name"))){
                criteria.andLike("nick_name","%"+searchMap.get("nick_name")+"%");
           	}
            // 真实姓名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 使用状态（1正常 0非正常）
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
           	}
            // 头像地址
            if(searchMap.get("head_pic")!=null && !"".equals(searchMap.get("head_pic"))){
                criteria.andLike("head_pic","%"+searchMap.get("head_pic")+"%");
           	}
            // QQ号码
            if(searchMap.get("qq")!=null && !"".equals(searchMap.get("qq"))){
                criteria.andLike("qq","%"+searchMap.get("qq")+"%");
           	}
            // 手机是否验证 （0否  1是）
            if(searchMap.get("is_mobile_check")!=null && !"".equals(searchMap.get("is_mobile_check"))){
                criteria.andLike("is_mobile_check","%"+searchMap.get("is_mobile_check")+"%");
           	}
            // 邮箱是否检测（0否  1是）
            if(searchMap.get("is_email_check")!=null && !"".equals(searchMap.get("is_email_check"))){
                criteria.andLike("is_email_check","%"+searchMap.get("is_email_check")+"%");
           	}
            // 性别，1男，0女
            if(searchMap.get("sex")!=null && !"".equals(searchMap.get("sex"))){
                criteria.andLike("sex","%"+searchMap.get("sex")+"%");
           	}

            // 会员等级
            if(searchMap.get("userLevel")!=null ){
                criteria.andEqualTo("userLevel",searchMap.get("userLevel"));
            }
            // 积分
            if(searchMap.get("points")!=null ){
                criteria.andEqualTo("points",searchMap.get("points"));
            }
            // 经验值
            if(searchMap.get("experienceValue")!=null ){
                criteria.andEqualTo("experienceValue",searchMap.get("experienceValue"));
            }

        }
        return example;
    }

}
