package com.example.router.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yangximin on 2019/8/29.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Route {

    String path();

    String group() default "";
}
