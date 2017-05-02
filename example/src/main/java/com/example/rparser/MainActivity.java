package com.example.rparser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.annotations.rparser.Parse;

public class MainActivity extends AppCompatActivity {

    @Parse(R.layout.activity_main) Object layout;
    @Parse(R.string.app_name) Object string;
    @Parse(R.mipmap.ic_launcher) Object mipmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}