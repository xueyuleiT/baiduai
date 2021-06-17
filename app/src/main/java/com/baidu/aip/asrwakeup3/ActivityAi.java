package com.baidu.aip.asrwakeup3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.aip.asrwakeup3.core.inputstream.InFileStream;
import com.baidu.aip.asrwakeup3.core.util.MyLogger;

/**
 * Created by fujiayi on 2017/6/20.
 */

public abstract class ActivityAi extends AppCompatActivity {

    protected EditText etHeyue;
    protected EditText etKekuan;
    protected EditText etCommend;
    protected Button btnStart;

    protected Handler handler;

    protected int layout;


    public ActivityAi() {
        this(R.layout.activity_test);
    }

    public ActivityAi(int layout) {
        super();
        this.layout = layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStrictMode();
        InFileStream.setContext(this);
        setContentView(layout);
        initView();
        handler = new Handler() {

            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }

        };
        MyLogger.setHandler(handler);
    }

    protected void handleMsg(Message msg) {
    }

    protected void initView() {
        etHeyue = findViewById(R.id.et_heyue);
        etKekuan = findViewById(R.id.et_kekuan);
        etCommend = findViewById(R.id.et_commend);
        btnStart = findViewById(R.id.btn_start);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

}
