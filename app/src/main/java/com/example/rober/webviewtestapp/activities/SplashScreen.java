package com.example.rober.webviewtestapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.rober.webviewtestapp.R;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        TestFairy.begin(this, "14f9d2f78928cf7b85e08b0c5a3d8467b27cd3f5"); // e.g "0000111122223333444455566667777788889999";
        //setContentView(R.layout.activity_splash);
        Log.i("MyMessageOnCreate", "now the app started and splash screen shows itself for 4 seconds");
        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

    }
}