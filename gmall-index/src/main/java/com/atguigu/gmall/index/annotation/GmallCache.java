package com.atguigu.gmall.index.annotation;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.METHOD})//定义这个注解可以使用在方法上面
@Retention(RetentionPolicy.RUNTIME)//定义这个注解在运行是使用
//@Inherited  子类可以继承
@Documented
public @interface GmallCache {

    //定义缓存的前缀
    String prefix() default "cache";

}
