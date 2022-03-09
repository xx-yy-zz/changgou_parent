package com.changgou.feign;

import com.changgou.entity.Result;
import com.changgou.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user")
public interface UserFeign {


    /**
     * 发送短信验证码
     * @param mobile
     * @return
     */
    @GetMapping("/user/send_sms")
    public Result sendSms( @RequestParam("mobile") String mobile);


    /**
     * 手机注册
     * @param user
     * @param smsCode
     * @return
     */
    @PostMapping("/user/add")
    public Result add(@RequestBody User user, @RequestParam("smsCode")  String smsCode);

}
