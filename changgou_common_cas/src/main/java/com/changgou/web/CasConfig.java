package com.changgou.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cas")
public class CasConfig {

    private String serverUrl;//cas服务端地址
    private String clientUrl;//cas客户端地址
    private List<String>  authUrlList; //需要认证的地址列表

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public List<String> getAuthUrlList() {
        return authUrlList;
    }

    public void setAuthUrlList(List<String> authUrlList) {
        this.authUrlList = authUrlList;
    }
}
