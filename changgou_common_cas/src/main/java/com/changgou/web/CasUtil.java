package com.changgou.web;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * cas工具类
 */
public class CasUtil {

    public static String loginName(){
       return  SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
