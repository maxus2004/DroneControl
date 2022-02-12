package com.example.dronecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onStop() {
        super.onStop();
        new Thread(()-> {
            EditText ipText = findViewById(R.id.droneIPText);
            EditText dronePortText = findViewById(R.id.dronePortText);
            EditText localPortText = findViewById(R.id.localPortText);
            IPConnection ipConnection = MainActivity.ipConnection;
            ipConnection.setDroneIP(ipText.getText().toString());
            ipConnection.setDronePort(Integer.parseInt(dronePortText.getText().toString()));
            ipConnection.setLocalPort(Integer.parseInt(localPortText.getText().toString()));
            ipConnection.reconnect();
        }).start();
    }
}