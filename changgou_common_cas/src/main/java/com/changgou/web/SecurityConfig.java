package com.changgou.web;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity //禁用Boot的默认Security配置，配合@Configuration启用自定义配置（需要扩展WebSecurityConfigurerAdapter）
@EnableGlobalMethodSecurity(prePostEnabled = true) //启用Security注解，例如最常用的@PreAuthorize
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CasConfig casConfig;  //cas配置

	/**
	 * configure(AuthenticationManagerBuilder): 身份验证配置，用于注入自定义身份验证Bean和密码校验规则
	 * @param auth
	 * @throws Exception
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		super.configure(auth);
		auth.authenticationProvider(casAuthenticationProvider());
	}

	/**
	 * configure(WebSecurity): Web层面的配置，一般用来配无需安全检查的置路径
	 * @param web
	 * @throws Exception
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/static/**", "/templates/**");
	}

	/**
	 * configure(HttpSecurity): Request层面的配置，对应XML Configuration中的<http>元素
	 * @param http
	 * @throws Exception
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		//需要认证的地址列表  集合转换为数组
		String[] authUrls = new String[casConfig.getAuthUrlList().size()];
		casConfig.getAuthUrlList().toArray( authUrls );

		http.authorizeRequests()//配置安全策略
				.antMatchers("/").permitAll()//所有请求都不需要验证
				.antMatchers(authUrls).authenticated()//admin下请求需要验证
				.and()
				.logout()
				.permitAll()//定义logout不需要验证
				.and()
				.formLogin();

		http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint())
				.and()
				.addFilter(casAuthenticationFilter())
				.addFilterBefore(casLogoutFilter(), LogoutFilter.class)
				.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);

		//http.csrf().disable();
		// 关闭spring security默认的frame访问限制
		//http.headers().frameOptions().sameOrigin();
	}

	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(casConfig.getServerUrl()+"/login");
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}

	/**
	 * 指定service相关信息
	 */
	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties serviceProperties = new ServiceProperties();
		//service 配置自身工程的根地址+/login/cas
		serviceProperties.setService(casConfig.getClientUrl()+"/login/cas");
		serviceProperties.setAuthenticateAllArtifacts(true);
		return serviceProperties;
	}

	/**
	 * CAS认证过滤器
	 */
	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		return casAuthenticationFilter;
	}

	/**
	 * cas 认证 Provider
	 */
	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setUserDetailsService(customUserDetailsService()); //这里只是接口类型，实现的接口不一样，都可以的。
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
		casAuthenticationProvider.setKey("an_id_for_this_auth_provider_only");
		return casAuthenticationProvider;
	}

    @Bean
    public UserDetailsService customUserDetailsService(){
        return new UserDetailsServiceImpl();
    }

	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		return new Cas20ServiceTicketValidator(casConfig.getServerUrl());
	}

	/**
	 * 单点登出过滤器
	 */
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		singleSignOutFilter.setCasServerUrlPrefix(casConfig.getServerUrl());
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		return singleSignOutFilter;
	}

	/**
	 * 请求单点退出过滤器
	 */
	@Bean
	public LogoutFilter casLogoutFilter() {
		LogoutFilter logoutFilter = new LogoutFilter(casConfig.getServerUrl()+"/logout", new SecurityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl("/logout");
		return logoutFilter;
	}
}

