package com.atguigu.gmall.cart.config;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "gmall.jwt")
public class JwtProperties {

    private String pubKeyPath; //有了公钥还需要有公钥对象

    private PublicKey publicKey; //公钥对象

    private String cookieName;

    private Integer expire;

    private String userKeyName;

    @PostConstruct
    public void init(){

        try {
            //根据公钥路径生成公钥对象
            PublicKey publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("购物车--初始化公钥失败!!");
            throw new RuntimeException();
        }

    }


}
