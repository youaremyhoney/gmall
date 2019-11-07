package com.atguigu.gmall.pms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SercurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //authorizeRequests()表示请求授权
        //antMatchers(String Path)  表示以Ant风格去匹配
        //permitAll()  表示放行所配置的路径
        http.authorizeRequests().antMatchers("/**").permitAll();
        //禁用csrf
        http.csrf().disable();
    }
}
