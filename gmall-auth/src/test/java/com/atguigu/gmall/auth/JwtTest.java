package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    public static final String pubKeyPath = "D:\\tmp\\rsa\\rsa.pub";

    public static final String priKeyPath = "D:\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    //生成公钥和私钥的方法，使用前要将@Before注释掉，因为会读取本地，生成的时候还没有
//    @Test
//    public void testRsa() throws Exception {
//        RsaUtils.generateKey(pubKeyPath,priKeyPath,"jdalkghdlk");
//    }

    //junit前置方法
    @Before
    public void testGetRsa() throws Exception {
        //从本地文件中读取公钥，生成一个公钥对象
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
        //从本地文件中读取私钥，生成一个私钥对象
        privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    //测试生成tok@Testen  -- 利用私钥生成token

    @Test
    public void testGenerateToken() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzM2MzQ2NTR9.avjG_MSjNYEHISDnoeyf5zcAVyAzCGTqEj6rt_b2f_hTZpWp5925zsYblZ3IVuuUZR4GqELIUvrXaobvnedxiHXnYTInP0MCi9oghwSSiqxVGCfrKsjLHoYz8lTorAyCVvhHhH4zhgOdtnUIBE5K9ZUKNE1dBbrnw3IhW8ZiUCf1S4dp4fEwv2iT_97GUGbDZwXf4Bd8SGGDr5wD1hHOuVbNv4hzwJD1FuM1eEk-wHn6_TVn9gppnIBGAh8gDqAaSEo62tLnoF4owL7-8COFPgEmSqP7OgufZL9YBAfhCJeh1G4jczkp4wXgf6GE5zfwktCaY0lSNwMcD_kqUR5qGg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }

}
