package com.liuyuan.nyy.util;

import android.util.Log;
import android.widget.Toast;

import com.friendlyarm.AndroidSDK.HardwareControler;

/**
 * Created by liuyuan on 2017/4/24.
 */

public class PWMUtil{
    private static String TAG = PWMUtil.class.getSimpleName();
    private HardwareControler mHardwareControler = new HardwareControler();
    private int frequency = 1000;

    public void palyPWM() {
        if (mHardwareControler.PWMPlay(frequency) != 0) {
            Log.d(TAG,"PWM fail");
        }
    }

    public void stopPWM() {
        if (mHardwareControler.PWMStop() != 0) {
            Log.d(TAG,"PWM fail");
        }
    }
}
