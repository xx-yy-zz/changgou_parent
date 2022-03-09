package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.feign.UserFeign;
import com.changgou.pojo.User;
import com.changgou.web.CasUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserFeign userFeign;

    /**
     * 发送短信验证码
     * @param mobile
     * @return
     */
    @GetMapping("/send_sms")
    public Result sendSms(String mobile){
        userFeign.sendSms(mobile);
        return new Result(  );
    }

    /**
     * 注册
     * @param user
     * @param smsCode
     * @return
     */
    @PostMapping("/add")
    public Result add( @RequestBody User user , String smsCode){
        userFeign.add( user,smsCode );
        return new Result(  );
    }


    /**
     * 获取当前登录名
     * @return
     */
    @GetMapping("/loginName")
    public Result loginName(){
        String loginName = CasUtil.loginName();
        return new Result( true, StatusCode.OK, loginName);
    }


}
