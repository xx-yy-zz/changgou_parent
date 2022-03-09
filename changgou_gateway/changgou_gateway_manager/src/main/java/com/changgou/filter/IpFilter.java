package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
public class IpFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //得到请求对象
        ServerHttpRequest request = exchange.getRequest();
        String ip = request.getRemoteAddress().getHostName();
        System.out.println("ip:"+ip);
        return chain.filter( exchange );
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
