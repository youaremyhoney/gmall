package com.atguigu.gmallgateway.config;

import com.atguigu.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilter implements GatewayFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取request和response 不是httpserverlet  而是webflux
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取所有的cookie
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        //如果cookie为空或者不包含指定的cookie，那么认证未通过
        if (CollectionUtils.isEmpty(cookies) || !cookies.containsKey(this.jwtProperties.getCookieName())){
                //响应为未认证
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                //结束请求
                return response.setComplete();
        }

        //获取cookie
        HttpCookie cookie = cookies.getFirst(this.jwtProperties.getCookieName());

        try {
            //校验cookie
            JwtUtils.getInfoFromToken(cookie.getValue(), this.jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
        //校验失败，认证未通过
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();

        }

            //校验成功，认证通过并放行
        return chain.filter(exchange);
    }
}
