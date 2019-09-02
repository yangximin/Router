package com.yang.test_module;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.router.annotation.Route;
import com.example.router.core.Router;

/**
 * Created by YXM
 * on 2019/8/30.
 */
@Route(path = "/test/test1")
public class TestActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yang_test_activity);
        findViewById(R.id.test_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().build("/main/home").navigation(TestActivity.this);
            }
        });
    }
}
