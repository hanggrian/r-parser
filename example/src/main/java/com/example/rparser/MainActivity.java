package com.example.rparser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.annotations.rparser.MyAnnotation;

public class MainActivity extends AppCompatActivity {

    @MyAnnotation(R.layout.activity_main) Object a;
    @MyAnnotation(R.string.app_name) Object b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}