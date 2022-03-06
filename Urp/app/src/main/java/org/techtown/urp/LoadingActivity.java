package org.techtown.urp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
    }

    //if you thouch the screen, change to the next screen
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onTouchEvent(event);
    }
}