package com.example.router.core.template;

import com.example.router.annotation.model.RouteMeta;

import java.util.Map;

/**
 * Created by YXM
 * on 2019/8/30.
 */

public interface IRouteGroup {

    void  loadInto(Map<String,RouteMeta> routes);
}
