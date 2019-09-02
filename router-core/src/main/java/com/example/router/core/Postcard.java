package com.example.router.core;

import android.content.Context;
import android.os.Bundle;

import com.example.router.annotation.model.RouteMeta;

/**
 * Created by YXM
 * on 2019/9/2.
 */

public class Postcard extends RouteMeta {

    private Bundle mBundle;

    private int flages = -1;


    public Postcard(String path, String group) {
        setPath(path);
        setGroup(group);
    }

    public void navigation(Context context) {
        Router.getInstance().navigation(context, this);
    }
}
