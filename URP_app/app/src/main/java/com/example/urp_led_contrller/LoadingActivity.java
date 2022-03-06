package com.example.urp_led_contrller;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class LoadingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        startLoading();
    }

    protected void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
