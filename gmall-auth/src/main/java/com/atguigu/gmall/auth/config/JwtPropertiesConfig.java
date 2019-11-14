package com.atguigu.gmall.auth.config;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "gmall.jwt")
public class JwtPropertiesConfig {

    private String pubKeyPath; // 公钥的路径

    private String priKeyPath; // 私钥的路径

    private String secret; // 盐

    private Integer expire; // 过期时间

    private String cookieName; //cookie的名称

    private PublicKey publicKey; // 公钥对象

    private PrivateKey privateKey; // 私钥对象

    //因为只有公钥和私钥文件还有路径，所以我们需要初始化私钥和公钥对象  -- 要使用在构造器方法执行之后的注解 @poseConstruct
    @PostConstruct
    public void init(){
        try {
            File pubkey = new File(pubKeyPath);
            File prikey = new File(priKeyPath);
            //如果公钥或者私钥有一个不存在，那么我们需要重新初始化公钥和私钥
            if ( !pubkey.exists() || !prikey.exists()){
                RsaUtils.generateKey(pubKeyPath, priKeyPath ,secret);
            }
            privateKey = RsaUtils.getPrivateKey(priKeyPath); // 生成私钥对象
            publicKey = RsaUtils.getPublicKey(pubKeyPath); // 生成公钥对象
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败",e);
            throw new RuntimeException();
        }
    }

}
