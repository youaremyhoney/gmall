package com.atguigu.gmall.index.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        //默认连接地址是127.0.0.1:6379
        RedissonClient redissonClient = Redisson.create();
        Config config = new Config();
        //可以使用redis://来启用ssl连接
        config.useSingleServer().setAddress("redis://192.168.16.128:6379");
        return Redisson.create(config);
    }

}
