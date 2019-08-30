package com.example.router.core.template;

import java.util.Map;

/**
 * Created by YXM
 * on 2019/8/30.
 */

public interface IRouteRoot {

    void  loadInto(Map<String,Class<? extends  IRouteGroup>> routes);
}
