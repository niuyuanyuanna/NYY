package com.liuyuan.nyy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.liuyuan.nyy.regist.RegistActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Intent intent;
    private ImageView round1;
    private ImageView round2;
    private ImageView round3;

    private Animation mAnimation1;
    private Animation mAnimation2;
    private Animation mAnimation3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUi();
    }

    private void initUi() {
        round1 = (ImageView) findViewById(R.id.round1);
        round2 = (ImageView) findViewById(R.id.round2);
        round3 = (ImageView) findViewById(R.id.round3);

        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btnMixVerify).setOnClickListener(this);

        //动画
        mAnimation1 = AnimationUtils.loadAnimation(this,
                R.anim.anim_scale1);
        round1.startAnimation(mAnimation1);
        mAnimation2 = AnimationUtils.loadAnimation(this,
                R.anim.anim_scale2);
        round2.startAnimation(mAnimation2);
        mAnimation3 = AnimationUtils.loadAnimation(this,
                R.anim.anim_scale3);
        round3.startAnimation(mAnimation3);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                intent = new Intent(this, RegistActivity.class);
                break;
            case R.id.btnMixVerify:
                intent = new Intent(this,MixVerifyActivity1.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }


}
