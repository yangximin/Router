package com.example.yangximin.arouter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.router.annotation.Route;
import com.example.router.core.Router;

@Route(path = "/main/home")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Router.init(this.getApplication());
        findViewById(R.id.main_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().build("/test/test1").navigation(MainActivity.this);
//                ARouter.getInstance().build("").navigation();
            }
        });
    }
}
