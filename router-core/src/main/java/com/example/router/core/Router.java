package com.example.router.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.router.annotation.model.RouteMeta;
import com.example.router.core.template.IRouteGroup;
import com.example.router.core.template.IRouteRoot;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;


/**
 * Created by yangximin on 2019/9/1.
 */

public class Router {

    private static final String TAG = "Router";
    private static final String ROUTE_ROOT_PAKCAGE = "com.example.router.routes";
    private static final String SDK_NAME = "Router";
    private static final String SEPARATOR = "$$";
    private static final String SUFFIX_ROOT = "Root";

    private Handler mHandler;

    private static Router instance;

    private static Application mContext;

    private Router() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static Router getInstance() {
        synchronized (Router.class) {
            if (instance == null) {
                instance = new Router();
            }
        }
        return instance;
    }

    public static void init(Application application) {
        mContext = application;
        try {
            loadInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadInfo() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, PackageManager.NameNotFoundException, InterruptedException {
        Set<String> routerMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);
        for (String className : routerMap) {
            if (className.startsWith(ROUTE_ROOT_PAKCAGE + "." + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                ((IRouteRoot) (Class.forName(className).getConstructor().newInstance()))
                        .loadInto(Warehouse.groupIndex);
            }
        }

//        Set<Map.Entry<String, Class<? extends IRouteGroup>>> entries = Warehouse.groupIndex.entrySet();
        for (Map.Entry<String, Class<? extends IRouteGroup>> entry : Warehouse.groupIndex.entrySet()) {
            Log.e(TAG, "Root映射表[ " + entry.getKey() + " : " + entry
                    .getValue() + "]");
        }

    }

    public Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("路由地址无效");
        } else {
            return build(path, extractGroup(path));
        }
    }

    private Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new RuntimeException("路由地址无效");
        } else {
            return new Postcard(path, group);
        }

    }

    public void navigation(final Context context, Postcard postcard) {
        try {
            prepareCard(postcard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (postcard.getType()) {
            case ACTIVITY:
                final Intent intent = new Intent(context, postcard.getDestination());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ActivityCompat.startActivity(context, intent, null);
                    }
                });
                break;
        }
    }

    private void prepareCard(Postcard postcard) throws Exception {
        RouteMeta routeMeta = Warehouse.routes.get(postcard.getPath());
        if (routeMeta == null) {
            Class<? extends IRouteGroup> groupMeta = Warehouse.groupIndex.get(postcard.getGroup());
            if (groupMeta == null) {
                throw new Exception("没找到对应路由: " + postcard.getGroup() + " " +
                        postcard.getPath());
            }
            IRouteGroup iRouteGroup;
            iRouteGroup = groupMeta.getConstructor().newInstance();
            iRouteGroup.loadInto(Warehouse.routes);
            Warehouse.groupIndex.remove(postcard.getGroup());
            prepareCard(postcard);
        } else {
            postcard.setDestination(routeMeta.getDestination());
            postcard.setType(routeMeta.getType());
        }
    }

    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException(path + " : 不能提取group.");
        }
        String defaultGroup = path.substring(1, path.indexOf("/", 1));
        if (TextUtils.isEmpty(defaultGroup)) {
            throw new RuntimeException(path + " : 不能提取group.");
        }
        return defaultGroup;
    }
}
