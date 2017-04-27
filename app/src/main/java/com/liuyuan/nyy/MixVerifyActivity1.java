package com.liuyuan.nyy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.friendlyarm.AndroidSDK.HardwareControler;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.cloud.util.VerifierUtil;
import com.liuyuan.nyy.ui.RecordView;
import com.liuyuan.nyy.util.DensityUtil;
import com.liuyuan.nyy.util.SaveFuncUtil;
import com.liuyuan.nyy.util.TtsFuncUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MixVerifyActivity1 extends AppCompatActivity
        implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = MixVerifyActivity1.class.getSimpleName();

    //声纹验证是否通过
    private boolean mLoginSuccess_v = false;
    private String vocalResult;

    private String name_f;
    private String name_v;
    private Double score_f;
    private Double score_v;
    private Bitmap mBitmap;

    private Camera mCamera;
//    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//    private Camera.Size mPreviewSize;
    private boolean mIsPreviewing = false;
    private boolean mCanTakePic = true;
    private boolean mIsPause = false;
    private Camera.AutoFocusCallback mAutoFocusCallback = null;
    // 图片压缩质量
    private static int JPEGQuality = 80;
//    private CameraHelper2 mCameraHelper;

    //相机预览SuifaceView
    private SurfaceView mPreviewSurface;
    private TextView mPwdTextView;
    private TextView tvGroupId;
    private ImageButton mFlashSwitchButton;
    private ImageButton mChangeCameraButton;
    private ImageButton mInputPwdButton;
    private ImageView mBtnRecord;
    private ProgressDialog mProDialog;
    private RecordView mVolView;
    //    private boolean mRecordButtonPressed;
    //提示框显示位置的纵坐标
    private int mHintOffsetY;

    // 用户输入的组ID
    private String mGroupId;
    private String mGroupName;

    //身份验证对象
    private IdentityVerifier mIdVerifier;
    //验证数字密码
    private String mVerifyNumPwd;
    //验证密码类型，3：数字密码，1：文字密码
    private static final String PWD_TYPE_NUM = "3";
    // 录音尾端点
    private static final String VAD_EOS = "2000";
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;

    // 是否开始验证
    private boolean mVerifyStarted = false;
    // 操作是否被其他应用中断
    private boolean mInterruptedByOtherApp = false;
    // 按住麦克风为true开始写音频，松开为false停止写入
    private boolean mWriteAudio = false;

    private final int DELAY_TIME = 1000;


    private static final int MSG_FACE_START = 1;
    private static final int MSG_TAKE_PICTURE = 2;
    private static final int MSG_PCM_START = 3;
    private static final int MSG_PCM_STOP = 4;
    private static final int MSG_RESULT_DISMISS = 5;
    private static final int MSG_ACTIVITY_FINISH = 6;

    private AlertDialog resultDialog = null;
    private AlertDialog.Builder mBuider = null;
    private Toast mToast;

    //语音合成类
    private TtsFuncUtil mTtsFuncUtil = new TtsFuncUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_verify);

        initUi();

        mIdVerifier = IdentityVerifier.createVerifier(getApplicationContext(), new InitListener() {
            @Override
            public void onInit(int i) {
                if (ErrorCode.SUCCESS == i) {
                    showTips("引擎初始化成功");
                } else
                    showTips(getString(R.string.login_hint_engine_error) + i);
            }
        });
    }

    private void initUi() {

        mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
        mPreviewSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //设置屏幕常亮
        mPreviewSurface.getHolder().setKeepScreenOn(true);
        //surfaceView增加回调句柄
        mPreviewSurface.getHolder().addCallback(this);
        mAutoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {

            }
        };

        tvGroupId = (TextView) findViewById(R.id.tv_group_name);
        mPwdTextView = (TextView) findViewById(R.id.txt_num);
        mFlashSwitchButton = (ImageButton) findViewById(R.id.btn_flash_switch);
        mChangeCameraButton = (ImageButton) findViewById(R.id.btn_change_camera);
        mInputPwdButton = (ImageButton) findViewById(R.id.btn_input_password);
        mBtnRecord = (ImageView) findViewById(R.id.btn_record);

        //设置组ID
        mGroupId = SpeechApp.getGroup_id();
        mGroupName = SpeechApp.getGroup_name();
        tvGroupId.setText(mGroupName);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setCanceledOnTouchOutside(false);
        mProDialog.setTitle(getString(R.string.login_predialog_title));
//        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                mIdVerifier.cancel();
//                mVerifyStarted = false;
//            }
//        });

        mChangeCameraButton.setOnClickListener(this);
        mFlashSwitchButton.setOnClickListener(this);
        mInputPwdButton.setOnClickListener(this);
        mToast = Toast.makeText(MixVerifyActivity1.this, "", Toast.LENGTH_SHORT);

        mVolView = new RecordView(MixVerifyActivity1.this);

        // turn off all led
        HardwareControler.setLedState(0,0);
        HardwareControler.setLedState(1,0);
        HardwareControler.setLedState(2,0);
        HardwareControler.setLedState(3,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取和设置验证密码
        mVerifyNumPwd = VerifierUtil.generateNumberPassword(8);
        mPwdTextView.setText(mVerifyNumPwd);

        mIsPause = false;
        //语音合成操作提示
        mTtsFuncUtil.ttsFunction(this, getString(R.string.login_operation_hint) +
                getStyledPwdHint(mVerifyNumPwd));

        mCanTakePic = true;
        if (mCamera != null) {
            mCamera.startPreview();
            mIsPreviewing = true;
        }
        //设置界面为横屏
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //打开Activity后延迟6S打开麦克风
        mHandler.sendEmptyMessageDelayed(MSG_PCM_START, 6000);
        //打开Activity后延迟13S关闭麦克风
        mHandler.sendEmptyMessageDelayed(MSG_PCM_STOP, 13000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change_camera:
                int cameracount = Camera.getNumberOfCameras();
                if (cameracount <= 1) {
                    showTips(getString(R.string.hint_change_not_support));
                    return;
                }
                break;
            case R.id.btn_flash_switch:
                // 检查当前硬件设施是否支持闪光灯
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    showTips(getString(R.string.hint_flash_not_support));
                    return;
                }
                Camera.Parameters param = mCamera.getParameters();
                String flasemode = param.getFlashMode();
                if (TextUtils.isEmpty(flasemode))
                    return;
                if (flasemode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mFlashSwitchButton.setImageResource(R.drawable.ico_flash_off);
                    showTips(getString(R.string.hint_flash_closed));
                } else {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mFlashSwitchButton.setImageResource(R.drawable.ico_flash_on);
                    showTips(getString(R.string.hint_flash_opened));
                }
                // 防止参数设置部分手机failed
                try {
                    mCamera.setParameters(param);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_input_password:
                //跳转到输入密码页面
                Intent intent = new Intent(this, InputPwdActivity.class);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }

    }

    private void actionDown() {
//        mRecordButtonPressed = true;
        // 按下事件
        if (!mVerifyStarted) {
            if (null != mCamera && !mIsPreviewing) {
                mCamera.startPreview();
                mIsPreviewing = true;
            }
            if (!mVerifyStarted) {
                mWriteAudio = true;
                // 开启录音机
                mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
                try {
                    mPcmRecorder.startRecording(mPcmRecordListener);
                } catch (SpeechError e) {
                    e.printStackTrace();
                }
                // 开始验证
                startMFVVerify();
            }

        } else {
            showTips(getString(R.string.login_hint_verifying));
        }
    }

    private void actionUp() {
//        mRecordButtonPressed = false;
        // 停止录音，开始拍照，随后开始人脸验证
        stopRecording();
        mHandler.sendEmptyMessageDelayed(MSG_TAKE_PICTURE, DELAY_TIME);
    }

    /**
     * 录音机监听器,向子业务传递参数
     */
    private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {

        @Override
        public void onRecordStarted(boolean success) {
            showTips("开始录音");
        }

        @Override
        public void onRecordReleased() {
        }

        @Override
        public void onRecordBuffer(byte[] data, int offset, int length) {
            if (mWriteAudio) {
                // 子业务执行参数，若无可以传空字符传
                StringBuffer params = new StringBuffer();
                params.append("ptxt=" + mVerifyNumPwd + ",");
                params.append("pwdt=" + PWD_TYPE_NUM + ",");
                params.append("group_id=" + mGroupId + ",");
                params.append("vad_eos=" + VAD_EOS + ",");
                params.append("topc = 1");

                Log.d(TAG, params.toString());
                // 向子业务写入声纹数据
                mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
            }
        }

        @Override
        public void onError(final SpeechError e) {
            // 由于onError不在主线程中回调，所以要runOnUiThread
            runOnUiThread(new Runnable() {
                public void run() {
                    showTip(e.getPlainDescription(true));
                }
            });
        }
    };

    /**
     * 开始声纹验证，Touch事件监听器中case MotionEvent.ACTION_DOWN处理
     * 设置声纹鉴别对象参数
     * 声纹鉴别监听器开始工作
     */
    private void startMFVVerify() {
        Log.d(TAG, "startMFVVerify");

        mVerifyStarted = true;
        mLoginSuccess_v = false;

        // 设置融合验证参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置组ID
        mIdVerifier.setParameter("group_id", mGroupId);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);

    }

    /**
     * 语音判别监听器，识别通过与否，(返回评分)
     */
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            mVolView.stopRecord();
            mVerifyStarted = false;

            if (null != mProDialog) {
                mProDialog.dismiss();
            }
            if (null == result) {
                return;
            }

            try {
                String resultStr_v = result.getResultString();
                JSONObject object = new JSONObject(resultStr_v);
                JSONObject ifv_result = object.getJSONObject("ifv_result");
                JSONArray candidates = ifv_result.getJSONArray("candidates");
                JSONObject obj = candidates.getJSONObject(0);

                int ret_v = object.getInt("ret");

                if (ErrorCode.SUCCESS != ret_v) {
                    showTips(getString(R.string.login_vocal_failure_hint));
                } else {
                    //声纹识别成功
                    if ((obj.optString("decision")).equals("accepted")) {
                        mLoginSuccess_v = true;
                        name_v = obj.optString("user");
                        score_v = obj.optDouble("score");

                        vocalResult = name_v + score_v;

                        // 保存到历史记录中
                        SpeechApp.getmHisList().addHisItem(object.getString("group_id"),
                                object.getString("group_name") + "(" + object.getString("group_id") + ")");
                        SaveFuncUtil.saveObject(MixVerifyActivity1.this, SpeechApp.getmHisList(),
                                SpeechApp.HIS_FILE_NAME);

                    } else {
                        mLoginSuccess_v = false;
                        vocalResult = getString(R.string.login_vocal_result_mismatch);
//                        setResultDialog(false, null, null, null,
//                                getString(R.string.login_vocal_result_mismatch));
//                        mHandler.removeMessages(MSG_FACE_START);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                mVolView.setVolume(arg1);
            }

            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

        @Override
        public void onError(SpeechError error) {
            mVerifyStarted = false;
            mLoginSuccess_v = false;
            mVolView.stopRecord();
            stopRecording();

            if (null != mProDialog) {
                mProDialog.dismiss();
            }
            vocalResult = error.getErrorDescription();
//            setResultDialog(false, null, null, null, error.getPlainDescription(true));
//            mHandler.removeMessages(MSG_FACE_START);
        }

    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (null == mVolView.getParent()) {
            // 设置VolView圆圈中心位置为麦克风中心
            Rect rect = new Rect();
            mBtnRecord.getHitRect(rect);
            mVolView.setCenterXY(rect.centerX(), rect.centerY());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            FrameLayout layout = (FrameLayout) findViewById(R.id.fllyt_vol);
            layout.addView(mVolView, 0, params);
        }

        if (hasFocus && !mLoginSuccess_v) {
            // 在合适的位置弹出提示框
            int[] loc = new int[2];
            mBtnRecord.getLocationInWindow(loc);
            mHintOffsetY = loc[1] - DensityUtil.dip2px(MixVerifyActivity1.this, 100);
            mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    0, mHintOffsetY);
        }

    }

    /**
     * 停止录音
     */
    private void stopRecording() {
        mWriteAudio = false;
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
        // 停止写入声纹数据
        mIdVerifier.stopWrite("ivp");

        setStopViewStatus();
    }

    private void setStopViewStatus() {
        if (null != mVolView) {
            mVolView.stopRecord();
            mVolView.clearAnimation();
        }
    }

    /**
     * 在8位数字密码中间加空格
     */
    private String getStyledPwdHint(String pwdHint) {
        String mTtsPwd = " ";
        for (int i = 0; i < pwdHint.length(); i++) {
            mTtsPwd = mTtsPwd + pwdHint.substring(i, i + 1) + " ";
        }
        pwdHint = mTtsPwd;
        return pwdHint;
    }

    /**
     * 人脸鉴别监听器，处理结果
     */
    private IdentityListener mSearchListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());
            mVerifyStarted = false;

            if (null != mProDialog) {
                mProDialog.dismiss();
            }
            if (null == result) {
                return;
            }
            try {
                String resultStr_f = result.getResultString();
                JSONObject object = new JSONObject(resultStr_f);
                JSONObject ifv_result = object.getJSONObject("ifv_result");
                JSONArray candidates = ifv_result.getJSONArray("candidates");
                JSONObject obj = candidates.getJSONObject(0);

                int ret_f = object.getInt("ret");
                if (ErrorCode.SUCCESS != ret_f) {
                    showTips(getString(R.string.login_face_failure_hint));
                }
                else if (mLoginSuccess_v) {
                    if ((obj.optString("decision")).equals("accepted")) {
                        name_f = obj.optString("user");
                        score_f = obj.optDouble("score");
                        // 保存到历史记录中
                        SpeechApp.getmHisList().addHisItem(object.getString("group_id"),
                                object.getString("group_name") + "(" + object.getString("group_id") + ")");
                        SaveFuncUtil.saveObject(MixVerifyActivity1.this, SpeechApp.getmHisList(),
                                SpeechApp.HIS_FILE_NAME);

                        showTips(getString(R.string.login_face_success_hint));

                        if (name_f.equals(name_v)) {
                            // 创建AlertDialog成功
                            setResultDialog(true, name_f, score_v, score_f, name_f);
//
                        } else {
                            // 创建AlertDialog失败
                            setResultDialog(false, name_f, score_v, score_f,
                                    getString(R.string.login_face_vocal_mismatch));
                        }
                    } else {
                        setResultDialog(false, name_f, score_v, score_f,
                                getString(R.string.login_face_result_mismatch));
                    }
                }
                else if(!mLoginSuccess_v){
                    if ((obj.optString("decision")).equals("accepted")) {
                        showTips(getString(R.string.login_face_success_hint));
                            // 创建AlertDialog失败
                            setResultDialog(false, null, null, null,
                                    getString(R.string.login_face_success_hint) +"\n"+ vocalResult);
                    } else {
                        setResultDialog(false,null, null, null,
                                getString(R.string.login_face_result_mismatch) +"\n"+ vocalResult);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            mVerifyStarted = false;

            if (null != mProDialog) {
                mProDialog.dismiss();
            }
            if(mLoginSuccess_v){
                setResultDialog(false,null,null,null,
                        getString(R.string.login_vocal_success_hint)+"\n"
                                + error.getErrorDescription());
            }else {
                setResultDialog(false,null,null,null,
                        vocalResult + "\n" + error.getErrorDescription());
            }

        }

    };

    /**
     * 识别结果响应
     * 存储照片以及结果
     *
     * @param decision
     * @param name
     * @param score_v
     * @param score_f
     */
    private void setResultDialog(Boolean decision, String name, Double score_v, Double score_f, String str) {
        mBuider = new AlertDialog.Builder(MixVerifyActivity1.this);

        //存储照片
        try {
            saveToSDCard(mBitmap, decision, str);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (decision) {
            resultDialog = mBuider.setIcon(R.drawable.icon_succeed)
                    .setTitle(getString(R.string.login_resultdialog_success))
                    .setMessage(name + ",请进" +
                            "\n声纹相似度：" + score_v +
                            "\n人脸相似度：" + score_f)
                    .create();
            resultDialog.show();
            mTtsFuncUtil.ttsFunction(MixVerifyActivity1.this,
                    getString(R.string.login_resultdialog_success) + str + "请进");
            HardwareControler.setLedState(0,1);
            mHandler.sendEmptyMessageDelayed(MSG_RESULT_DISMISS, 6000);
            mHandler.sendEmptyMessageDelayed(MSG_ACTIVITY_FINISH, 7000);
        } else {
            SpeechApp.failTime++;

            if (SpeechApp.failTime >= 3) {
                //提示输入密码
                resultDialog = mBuider.setIcon(R.drawable.icon_failed)
                        .setTitle(getString(R.string.login_resultdialog_fail))
                        .setMessage(getString(R.string.login_hint_input_pwd))
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MixVerifyActivity1.this, InputPwdActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).create();
                resultDialog.show();
                mTtsFuncUtil.ttsFunction(MixVerifyActivity1.this, getString(R.string.login_resultdialog_fail)
                        + getString(R.string.login_hint_input_pwd));
                SpeechApp.failTime = 0;
            } else {
                resultDialog = mBuider.setIcon(R.drawable.icon_failed)
                        .setTitle(getString(R.string.login_resultdialog_fail))
                        .setMessage("失败原因：\n" + str)
                        .create();
                resultDialog.show();
                mTtsFuncUtil.ttsFunction(MixVerifyActivity1.this,
                        getString(R.string.login_resultdialog_fail) + str);
                mHandler.sendEmptyMessageDelayed(MSG_RESULT_DISMISS, 8000);
                mHandler.sendEmptyMessageDelayed(MSG_ACTIVITY_FINISH, 9000);
            }

        }

    }

    /**
     * 将照片分类存放
     *
     * @param bitmap
     * @param decision
     * @param str
     * @throws IOException
     */
    private static void saveToSDCard(Bitmap bitmap, Boolean decision, String str) throws IOException {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        String filename = format.format(date) + str + ".jpg";
        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/NYY/"
                + "verifyData");
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"verifyData"的目录
            fileFolder.mkdir();
        }
        if (decision) {
            //识别结果成功则将照片放入succeed文件夹
            fileFolder = new File(Environment.getExternalStorageDirectory() + "/NYY/verifyData/"
                    + "succeed");
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"succeed"的目录
                fileFolder.mkdir();
            }
        } else {
            //识别结果失败则将照片放入failed文件夹
            fileFolder = new File(Environment.getExternalStorageDirectory() + "/NYY/verifyData/"
                    + "failed");
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"failed"的目录
                fileFolder.mkdir();
            }
        }

        File mapFile = new File(fileFolder, filename);
        FileOutputStream outputStream = new FileOutputStream(mapFile); // 文件输出流
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);// 把数据写入文件
        outputStream.flush();
        outputStream.close(); // 关闭输出流
    }

    /**
     * 处理消息，拍照或开始人脸识别
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FACE_START:
                    startFaceVerify();
                    showProDialog();
                    break;
                case MSG_TAKE_PICTURE:
                    takePicture();
                    break;
                case MSG_PCM_START:
                    actionDown();
                    break;
                case MSG_PCM_STOP:
                    actionUp();
                    break;
                case MSG_RESULT_DISMISS:
                    resultDialog.dismiss();
                    HardwareControler.setLedState(0,0);
                    break;
                case MSG_ACTIVITY_FINISH:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 开始拍照
     * 发起人脸验证
     */
    private void takePicture() {
        // 拍照，发起人脸注册
        try {
            if (mCamera != null && mCanTakePic) {
                Log.d(TAG, "takePicture");
                mCamera.takePicture(mShutterCallback, null, mPictureCallback);
                mCanTakePic = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发起人脸验证
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");

            Bitmap bitmap = null;
            if(null != data) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                mCamera.stopPreview();
                mIsPreviewing = false;
            }
            Matrix matrix = new Matrix();

            matrix.postRotate((float) 0.0);
            mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);


            if (!mIsPause ) {
                //发送消息 开始人脸识别
                mHandler.sendEmptyMessage(MSG_FACE_START);
            }
            mIsPreviewing = false;
            mCanTakePic = true;
        }
    };
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
        }
    };

    /**
     * 开始人脸验证
     * 设置参数
     * 向子业务写入人脸数据
     */
    private void startFaceVerify() {
        Log.d(TAG, "startFaceVerify");

        byte[] imageData = getImageData();

        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置业务场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 设置业务类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mSearchListener);

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params_f = new StringBuffer();

        params_f.append(",group_id=" + mGroupId + ",topc=1");

        // 向子业务写入数据，人脸数据可以一次写入
        mIdVerifier.writeData("ifr", params_f.toString(), imageData, 0, imageData.length);
        // 停止写入数据
        mIdVerifier.stopWrite("ifr");
    }

    private byte[] getImageData() {
        ByteArrayOutputStream baos = null;
        try {
            Matrix matrix = new Matrix();
            // 获取原始图片的宽和高度
            int orgWidth = mBitmap.getWidth();
            int orgHeight = mBitmap.getHeight();
            float scaleWidth = ((float) orgWidth) / 600;
            float scaleHeigth = ((float) orgHeight) / 800;
            float scale = Math.max(scaleWidth, scaleHeigth);
            matrix.postScale(1 / scale, 1 / scale);

            // 得到放缩后的图片
            Bitmap scaledBitmap = Bitmap.createBitmap(mBitmap, 0, 0, orgWidth, orgHeight, matrix, true);
            Log.d(TAG, "Image orgwidth:" + orgWidth + ",scaleWidth:" + scaleWidth
                    + ",orgHeight:" + orgHeight + ",scaleHeigth:" + scaleHeigth
                    + ",newWidth:" + scaledBitmap.getWidth() + ",newHeight:"
                    + scaledBitmap.getHeight());

            // 将调整后的图片转换成字节流
            baos = new ByteArrayOutputStream();
            byte[] bytes = null;

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEGQuality, baos);
            bytes = baos.toByteArray();

            recycleBitmap(scaledBitmap);
            scaledBitmap = null;
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                baos = null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 释放bitmap资源
     *
     * @param bitmap
     */
    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
//			bitmap = null;
        }
    }

    private void showProDialog() {
        mProDialog.setMessage("验证中...");
        mProDialog.show();
    }

    private void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
        } catch (IOException e) {
            if(null != mCamera){
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mIsPreviewing){
            mCamera.stopPreview();
        }
        if(null != mCamera){
            Camera.Parameters mParam = mCamera.getParameters();
            mParam.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式

            //设置分辨率，支持如下分辨率 "1280x720,1184x656,960x720,960x544,864x480,800x448,544x288,352x288,320x176"
            //预览和拍照的分辨率必须是相同的
            mParam.setPictureSize(1280, 720);
            mParam.setPreviewSize(1280, 720);
            mCamera.setDisplayOrientation(0);
            mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(mParam);
            mCamera.startPreview();
            mCamera.autoFocus(mAutoFocusCallback);
            mIsPreviewing = true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    @Override
    protected void onPause() {
        mIsPause = true;
        //停止语音合成
        mTtsFuncUtil.ttsCancel();
        // 关闭录音机
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }

        mHandler.removeMessages(MSG_FACE_START);
        mHandler.removeMessages(MSG_TAKE_PICTURE);
        mHandler.removeMessages(MSG_PCM_START);
        mHandler.removeMessages(MSG_PCM_STOP);
        mHandler.removeMessages(MSG_RESULT_DISMISS);
        mHandler.removeMessages(MSG_ACTIVITY_FINISH);

        // 若已经开始验证，然后执行了onPause就表明Activity被其他应用中断
        if (mVerifyStarted) {
            mInterruptedByOtherApp = true;
            finish();
        }

        mVerifyStarted = false;

        if (null != mIdVerifier) {
            mIdVerifier.cancel();
        }

        // 防止跳转到其他Activity后还显示前面的toast
        if (null != mToast) {
            mToast.cancel();
        }

        // 取消进度框
        if (null != mProDialog) {
            mProDialog.cancel();
        }

        setStopViewStatus();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }
    }

    @Override
    public void finish() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
        mTtsFuncUtil.ttsCancel();

        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }

        super.finish();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
        mTtsFuncUtil.ttsFunction(MixVerifyActivity1.this, str);
    }

    private void showTips(String str) {
        mToast.setText(str);
        mToast.show();
    }
}
