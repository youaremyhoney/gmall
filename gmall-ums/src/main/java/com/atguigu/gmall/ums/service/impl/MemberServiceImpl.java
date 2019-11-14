package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.utils.SendMessageUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;
import org.springframework.util.StringUtils;

import javax.xml.crypto.Data;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Autowired
    private MemberDao memberDao;

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        //因为需要验证的数据有三种情况 -- 所以我们使用switch
        switch (type) {
            case 1:
                wrapper.eq("username",data);
                break;
            case 2:
                wrapper.eq("mobile",data);
                break;
            case 3:
                wrapper.eq("email",data);
                break;
            default :
                return null;
        }

        return this.memberDao.selectCount(wrapper) == 0;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void register(MemberEntity memberEntity, String code) {

        String message = (memberEntity.getMobile() + ":" + code);

        //1.校验短信验证码
        String redisCode = this.stringRedisTemplate.opsForValue().get(message);

        if (redisCode == null){
            return;
        }

        if (!redisCode.equals(code)){
            return;
        }

        //2.生成随机盐
        String salt = UUID.randomUUID().toString().replace("-", "");

        //3.对密码进行加密加盐 然后设置成密码
        memberEntity.setPassword(DigestUtils.md5Hex(salt + DigestUtils.md5Hex(memberEntity.getPassword())));

        //设置创建时间
        memberEntity.setCreateTime(new Date());

        //4.将数据写入到数据库
        memberEntity.setSalt(salt);
        boolean b = this.save(memberEntity);

        //5.删除redis中存储的验证码
        if (b){
            //删除redis中验证码
            this.stringRedisTemplate.delete(message);
        }

    }

    /**
     * 根据用户名和密码查询用户是否存在
     * @param username
     * @param password
     * @return
     */
    @Override
    public MemberEntity queryUser(String username, String password) {
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        //因为密码加密，所以先根据用户名查询出唯一的用户(也可能不存在，有就是唯一)
        MemberEntity memberEntity = this.getOne(wrapper.eq("username", username));

        if (memberEntity == null) {
            return null;
        }

        //先获取盐
        String salt = memberEntity.getSalt();

        //然后将用户传过来的密码加密
        String md5Hex = DigestUtils.md5Hex(salt + DigestUtils.md5Hex(password));

        //将从数据库中查询的加盐加密后的密码拿出来
        String memberEntityPassword = memberEntity.getPassword();

        if (memberEntityPassword.equals(md5Hex)){
            return memberEntity;
        }
            return null;
    }



    /**
     * 根据用户的手机号码发送短信，并将短信存储到redis中并设置过期时间为5分钟
     * @param mobile
     * @return
     */
    @Override
    public String sendMsg(String mobile) {

        //生成一个6位数的随机验证码
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        Integer integer = SendMessageUtils.sendMsg(code, mobile);

        if (integer == 0){
            return "验证码发送失败，请稍后尝试!";
        }else {
            //如果验证码发送成功，将验证码存储到redis中
            this.stringRedisTemplate.opsForValue().set((mobile + ":" + code),code,5l, TimeUnit.MINUTES);
        return "验证码发送成功，有效时间为5分钟!";
        }

    }

}