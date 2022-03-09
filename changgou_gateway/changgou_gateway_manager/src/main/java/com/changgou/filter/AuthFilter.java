package com.changgou.filter;

import com.changgou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //1.取出request
        ServerHttpRequest request = exchange.getRequest();
        //2.取出respoonse
        ServerHttpResponse response = exchange.getResponse();
        //3.判断请求地址是不是登录地址，如果是登录地址，放行
        if( request.getURI().getPath().equals("/system/admin/login"  ) ){
            System.out.println("登录地址放行");
            return chain.filter( exchange );//放行
        }

        //4.提取token ( 请求头信息  token 与前端约定)
        String token = request.getHeaders().getFirst( "token" );
        //5.判断token 是否为空，如果为空，拦截
        if(Strings.isEmpty(token) ){  //  token==null || "".eques(token)
            response.setStatusCode( HttpStatus.UNAUTHORIZED );//401
            return response.setComplete();
        }

        //6.验证token 真伪（解析）
        try {
            Claims claims = JwtUtil.parseJWT( token );
            System.out.println(claims.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode( HttpStatus.UNAUTHORIZED );//401
            return response.setComplete();
        }
        return chain.filter( exchange );//放行
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
