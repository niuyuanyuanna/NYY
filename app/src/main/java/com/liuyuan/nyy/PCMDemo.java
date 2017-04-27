package com.liuyuan.nyy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.friendlyarm.AndroidSDK.HardwareControler;

public class PCMDemo extends AppCompatActivity {
    HardwareControler hw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcmdemo);

        hw = new HardwareControler();
    }
    private void playPWM()
    {
        int f;
        f = 1000;
        if (hw.PWMPlay(f) != 0) {
            System.out.println("fail");
        }
    }

    private void stopPWM()
    {
        if (hw.PWMStop() != 0) {
            System.out.println("fail");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
        playPWM();
    }

    @Override
    public void onPause() {
        stopPWM();
        super.onPause();
    }

    @Override
    public void onStop() {
        stopPWM();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopPWM();
        super.onDestroy();
    }
}
