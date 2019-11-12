package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Arrays;

@Component
@Aspect // 生命这个类是一个切面类
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 1.返回值是object
     * 2.参数是ProceedingJoinPoint
     * 3.抛出异常Throwable
     * 4.ProceedingJoinPoint.proceed(args)执行方法
     */

    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) throws Throwable{

        Object result = null;
        //获取连接点签名
        MethodSignature signature = (MethodSignature)point.getSignature();
        //获取连接点的GmallCache注解信息
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //获取缓存的前缀
        String prefix = gmallCache.prefix();

        //组装key
        String key = prefix + Arrays.asList(point.getArgs()).toString();

        //查询缓存
        result = this.cacheHit(signature, key);

        if (result != null){
            return result;
        }

        //初始化分布式锁
        RLock lock = this.redissonClient.getLock("gmallcache");
        //防止缓存穿透，加锁
        lock.lock();

        //再次检查内存中是否有缓存，防止加锁这段时间，已经有其他的线程放入缓存
        result = this.redisTemplate.opsForValue().get(key);
        if (result != null){
            //说明在加锁的这段时间里面，其他的线程已经放入缓存 -- 所以直接释放锁，然后返回缓存
            lock.unlock();
            return result;
        }

        //如果没有，执行数据库查询逻辑  -- 通过poin.proceed方法执行原始的方法
        result = point.proceed(point.getArgs());

        //将查询到的数据放入缓存
        this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result));

        //释放锁
        lock.unlock();

        return result;
    }

//查询缓存的方法
    private  Object cacheHit(MethodSignature signature,String key){
            //1.查询缓存
            String cache = this.redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(cache)){
                //有缓存，反序列化，直接返回
                Class returnType = signature.getReturnType();
                return JSON.parseObject(cache,returnType);
            }
        return null;
    }

}
