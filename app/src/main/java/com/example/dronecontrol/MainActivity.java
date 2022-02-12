package com.example.dronecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    static IPConnection ipConnection;
    int cameraMovingDirection;
    int cameraPosition = 90;
    boolean recording = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipConnection = new IPConnection(this);
        ipConnection.startStreaming();

        ((StreamView)findViewById(R.id.cameraView)).setIpConnection(ipConnection);

        findViewById(R.id.settingsBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.recordBtn).setOnClickListener(v -> {
            ImageButton btn = (ImageButton)v;
            if(recording) {
                ipConnection.stopRecording();
                recording = false;
                btn.setImageResource(android.R.drawable.presence_video_online);
            }else{
                ipConnection.startRecording();
                recording = true;
                btn.setImageResource(android.R.drawable.presence_video_busy);
            }
        });
        findViewById(R.id.takePictureBtn).setOnClickListener(v -> {
            ipConnection.takePicture();
        });

        findViewById(R.id.cameraUpBtn2).setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cameraMovingDirection = -1;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                cameraMovingDirection = 0;
            }
            return false;
        });
        findViewById(R.id.cameraDownBtn2).setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cameraMovingDirection = 1;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                cameraMovingDirection = 0;
            }
            return false;
        });

        new Thread(()->{
            while(true){
                if(cameraMovingDirection != 0){
                    cameraPosition+=cameraMovingDirection;
                    if(cameraPosition < 0)cameraPosition = 0;
                    if(cameraPosition > 180)cameraPosition = 180;
                    ipConnection.moveCamera(cameraPosition);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy(){
        ipConnection.disconnect();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowInsetsControllerCompat windowInsetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}