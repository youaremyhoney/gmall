package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtPropertiesConfig;
import com.atguigu.gmall.auth.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
@EnableConfigurationProperties(JwtPropertiesConfig.class)
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtPropertiesConfig jwtPropertiesConfig;
    @PostMapping("accredit")
    public Resp<Object> authentication(
            @RequestParam("username") String username,
            @RequestParam("password")String password,
            HttpServletRequest request,
            HttpServletResponse response){
        //登陆校验
        String token = this.authenticationService.authentication(username, password);
        if (StringUtils.isEmpty(token)){
           return  Resp.ok("登陆失败，用户名或者密码错误！！");
        }

        //将token写入cookie，防止通过js获取或修改
        CookieUtils.setCookie(request, response ,jwtPropertiesConfig.getCookieName(),token, jwtPropertiesConfig.getExpire());

        return Resp.ok("登陆成功");
    }

}
