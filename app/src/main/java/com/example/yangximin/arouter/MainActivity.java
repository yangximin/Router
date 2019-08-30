package com.example.yangximin.arouter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.router.annotation.Route;

@Route(path = "/main/home")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
