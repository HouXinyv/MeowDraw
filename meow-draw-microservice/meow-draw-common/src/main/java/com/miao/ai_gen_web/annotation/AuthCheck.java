package com.miao.ai_gen_web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) //Retention 的字面意思是 “保留” 或 “存活时间”
public @interface AuthCheck { //@interface 则是用于定义“注解”的关键字。

    /**
     * 必须有某个角色
     */
    String mustRole() default "";
}
