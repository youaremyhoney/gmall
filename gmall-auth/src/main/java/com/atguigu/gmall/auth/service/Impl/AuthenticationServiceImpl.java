package com.atguigu.gmall.auth.service.Impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtPropertiesConfig;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.service.AuthenticationService;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtPropertiesConfig jwtPropertiesConfig;

    @Override
    public String authentication(String username, String password) {

        try {
            //调用ums查询用户名和密码的方法
            Resp<MemberEntity> memberEntityResp = this.umsClient.queryUser(username, password);
            MemberEntity memberEntity = memberEntityResp.getData();

            //如果没有，直接返回
            if (memberEntity == null){
                return null;
            }

            //如果有，就直接生成token返回
            Map<String, Object> map = new HashMap<>();
            map.put("id",memberEntity.getId());
            map.put("username",memberEntity.getUsername());
            String token = JwtUtils.generateToken(map, jwtPropertiesConfig.getPrivateKey(), jwtPropertiesConfig.getExpire() * 60 * 60);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
