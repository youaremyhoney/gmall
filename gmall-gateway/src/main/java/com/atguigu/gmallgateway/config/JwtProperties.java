package com.atguigu.gmallgateway.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "gmall.jwt")
public class JwtProperties {

    private String pubKeyPath;

    private PublicKey publicKey;

    private String cookieName;

    @PostConstruct
    public PublicKey getPublicKey(){
        try {
            //获取公钥对象
            PublicKey publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("获取公钥失败！");
            throw new RuntimeException();
        }
    return publicKey;

    }

}
