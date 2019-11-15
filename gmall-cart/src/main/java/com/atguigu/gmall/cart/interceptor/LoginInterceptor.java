package com.atguigu.gmall.cart.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class) // 启用配置类
public class LoginInterceptor extends HandlerInterceptorAdapter {

    public static final ThreadLocal<UserInfo> THREAD_LOCAL =  new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    //预处理的方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {



        //获取cookie信息
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName()); // 获取token
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKeyName()); // 获取userKey

        //如果都为空，设置userKey信息
        if (StringUtils.isEmpty(token) && StringUtils.isEmpty(userKey)){
            userKey = UUID.randomUUID().toString(); // 用uuid来设置唯一标识
            //然后将userKey设置到cookie中
            CookieUtils.setCookie(request, response, jwtProperties.getUserKeyName(), userKey, jwtProperties.getExpire());
        }

        //不管有没有登陆，都要获取userKey
        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);

        //token不为空，解析token
        if (!StringUtils.isEmpty(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            if (CollectionUtils.isNotEmpty(map)) {
                userInfo.setUserId(Long.parseLong(map.get("id").toString()));
            }
        }

        //保存到ThreadLocal
        THREAD_LOCAL.set(userInfo);

        //如果token不为空
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //因为我们使用的是线程池，所以线程在使用完之后，不一定会销毁，那么我们将线程池里面线程的局部变量删除
        THREAD_LOCAL.remove();
    }

    //创建一个获取userInfo的方法来返回userInfo信息
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }
}
