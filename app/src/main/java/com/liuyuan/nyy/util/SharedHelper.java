package com.liuyuan.nyy.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/4/6.
 */

public class SharedHelper {
    private static final String TAG = SharedHelper.class.getSimpleName();
    private Context mContext;

    public SharedHelper(Context mContext) {
        this.mContext = mContext;
    }

    //定义一个保存用户名的方法
    public void saveUname(String username){
        SharedPreferences spUname = mContext.getSharedPreferences("spUname", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spUname.edit();
        editor.putString("username", username);
        editor.commit();
        Log.d(TAG,"用户名已写入SharedPreference中");
    }

    //定义一个保存组ID的方法
    public void saveId(String group_id) {
        SharedPreferences spid = mContext.getSharedPreferences("spid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spid.edit();
        editor.putString("group_id", group_id);
        editor.commit();
        Log.d(TAG,"组ID已写入SharedPreference中");
    }
    //定义一个保存密码的方法
    public void savePwd(String pwd){
        SharedPreferences sppwd = mContext.getSharedPreferences("sppwd", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sppwd.edit();
        editor.putString("password",pwd);
        editor.commit();
        Log.d(TAG,"密码已写入SharedPreference中");
    }

    //定义读取SP文件方法
    public String readUname(){
        String username = null;
        SharedPreferences mSharedPreference = mContext.getSharedPreferences("spUname", Context.MODE_PRIVATE);
        username = mSharedPreference.getString("username","");
        return username;
    }

    //定义一个读取SP文件的方法
    public String readId(){
        String groupId = null;
        SharedPreferences mSharedPreference = mContext.getSharedPreferences("spid", Context.MODE_PRIVATE);
        groupId = mSharedPreference.getString("group_id","");
        return groupId;
    }

    public String readPwd(){
        String groupPwd = null;
        SharedPreferences mSharedPreference = mContext.getSharedPreferences("sppwd", Context.MODE_PRIVATE);
        groupPwd = mSharedPreference.getString("password","");
        return groupPwd;
    }
}

