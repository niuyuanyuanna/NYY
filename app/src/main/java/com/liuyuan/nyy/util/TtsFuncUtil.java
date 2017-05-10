package com.liuyuan.nyy.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;

/**
 * Created by admin on 2017/3/16.
 */

public class TtsFuncUtil {
    private static String TAG = TtsFuncUtil.class.getSimpleName();
    private SpeechSynthesizer mTts;
    private static volatile TtsFuncUtil sInstance;

    private TtsFuncUtil() {}

    public static TtsFuncUtil getInstance() {
        if (sInstance == null) {
            synchronized (TtsFuncUtil.class) {
                if (sInstance == null) {
                    sInstance = new TtsFuncUtil();
                }
            }
        }
        return sInstance;
    }



    public void ttsFunction(Context context, String text ,TtsListener listener) {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);

        FlowerCollector.onEvent(context, "tts_play");
        setParam();
        int code = mTts.startSpeaking(text, listener);
        if (code != ErrorCode.SUCCESS)
            Log.d(TAG, "语音合成失败，错误码：" + code);
    }

    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);

        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        mTts.setParameter("ttp", "cssml");
        mTts.setParameter(SpeechConstant.TEXT_ENCODING, "GB2312");

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };


    //停止播放
    public void ttsCancel() {
        mTts.stopSpeaking();
    }
    public void ttsDestroy(){
        mTts.destroy();
    }
}
