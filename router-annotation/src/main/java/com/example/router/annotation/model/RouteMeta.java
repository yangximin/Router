package com.example.router.annotation.model;

import com.example.router.annotation.Route;

import javax.lang.model.element.Element;

/**
 * Created by yangximin on 2019/8/29.
 */

public class RouteMeta {
    public  enum Type{
        ACTIVITY,
        ISERVICE
    }

    private  Type type;
    /**
     * 节点 activity
     */
    private Element element;
    /**
     * 注解使用的类对象
     */
    private Class<?> destination;
    /**
     * 路由地址
     */
    private String path;
    /**
     * 路由组
     */
    private String group;

    public RouteMeta(Type type, Element element, Class<?> destination, String path, String group) {
        this.type = type;
        this.element = element;
        this.destination = destination;
        this.path = path;
        this.group = group;
    }

    public RouteMeta(Type type, Route route,Element element){
        this(type,element,null,route.path(),route.group());
    }
}
