package com.baidu.aip.asrwakeup3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;

import com.baidu.aip.asrwakeup3.controller.MySyntherizer;
import com.baidu.aip.asrwakeup3.controller.NonBlockSyntherizer;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.mini.InitConfig;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.core.util.MyLogger;
import com.baidu.aip.asrwakeup3.listener.UiMessageListener;
import com.baidu.aip.asrwakeup3.uiasr.params.CommonRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OnlineRecogParams;
import com.baidu.aip.asrwakeup3.util.Auth;
import com.baidu.aip.asrwakeup3.util.IOfflineResourceConst;
import com.baidu.aip.asrwakeup3.util.OfflineResource;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DigitalDialogInput;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.baidu.aip.asrwakeup3.core.recog.IStatus.*;
import static com.baidu.aip.asrwakeup3.util.IOfflineResourceConst.VOICE_FEMALE;

public class TestActivity extends AppCompatActivity {

//    SpeechSynthesizer mSpeechSynthesizer = SpeechSynthesizer.getInstance();

    private EditText etHeyue;
    private EditText etKekuan;
    private EditText etCommend;

    protected Handler handler;

    private CommonRecogParams apiParams = new OnlineRecogParams();
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */

    private DigitalDialogInput input;

    private ChainRecogListener chainRecogListener;


    protected MyRecognizer myRecognizer;

    /**
     * 控制UI按钮的状态
     */
    protected int status = STATUS_NONE;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        etHeyue = findViewById(R.id.et_heyue);
        etKekuan = findViewById(R.id.et_kekuan);
        etCommend = findViewById(R.id.et_commend);

        etHeyue.requestFocus();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String PID = sp.getString(SpeechConstant.PID,"1537");
        int id = R.id.rb_tab_1;
        if (PID.equals("1537")) {
            id = R.id.rb_tab_1;
        } else if (PID.equals("1637")) {
            id = R.id.rb_tab_2;
        } else if (PID.equals("1737")) {
            id = R.id.rb_tab_3;
        }
        ((RadioGroup)findViewById(R.id.radio_tab)).check(id);

        final List<String> list = new ArrayList<>();
        list.add(Permission.READ_EXTERNAL_STORAGE);
        list.add(Permission.WRITE_EXTERNAL_STORAGE);
        list.add(Permission.RECORD_AUDIO);
        XXPermissions.with(this)
                .permission(list) //不指定权限则自动获取清单中的危险权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (granted.size() == list.size()) {
                            init();
                        } else {
                            Toast.makeText(getBaseContext(),"请授权后使用",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {

                    }
        });

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                XXPermissions.with(TestActivity.this)
                        .permission(list) //不指定权限则自动获取清单中的危险权限
                        .request(new OnPermission() {
                            @Override
                            public void hasPermission(List<String> granted, boolean isAll) {
                                if (granted.size() == list.size()) {
                                    if (myRecognizer == null) {
                                        init();
                                    }
                                    switch (status) {
                                        case STATUS_NONE: // 初始状态
                                            start();
                                            status = STATUS_WAITING_READY;
                                            break;
                                        case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                                        case STATUS_READY: // 引擎准备完毕。
                                        case STATUS_SPEAKING: // 用户开始讲话
                                        case STATUS_FINISHED: // 一句话识别语音结束
                                        case STATUS_RECOGNITION: // 识别中
                                            stop();
                                            status = STATUS_STOPPED; // 引擎识别中
                                            break;
                                        case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                                        case STATUS_STOPPED: // 引擎识别中
                                            cancel();
                                            status = STATUS_NONE; // 识别结束，回到初始状态
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    Toast.makeText(getBaseContext(),"请授权后使用",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void noPermission(List<String> denied, boolean quick) {

                            }
                        });

            }
        });
    }

    private void init() {
        Auth.getInstance(this);

        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }

        };

        appId = Auth.getInstance(this).getAppId();
        appKey = Auth.getInstance(this).getAppKey();
        secretKey = Auth.getInstance(this).getSecretKey();
        sn = Auth.getInstance(this).getSn(); // 离线合成SDK必须有此参数；在线合成SDK没有此参数

        initialTts(); // 初始化TTS引擎
        initListener();


        apiParams.initSamplePath(this);

//        mSpeechSynthesizer.setContext(this);
//        mSpeechSynthesizer.setAppId("24329655");
//        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0"); // 设置发声的人声音，在线生效
//        mSpeechSynthesizer.setApiKey("AsKp2OBNg5U1IKGAWuYLejhq","XjNrei3AjBXnsvGZtizYwU1kNmCnaksV");
//        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUTH_SN, "82b2c6e3-524584b4-0e6a-0014-a358d-00");
//        mSpeechSynthesizer.initTts(TtsMode.OFFLINE);



        IRecogListener listener = new MessageStatusRecogListener(new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what ==  4) {
                }
            }

        });

        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        };

        myRecognizer = new MyRecognizer(this, listener);

        chainRecogListener = new ChainRecogListener();
        chainRecogListener.addListener(new MessageStatusRecogListener(handler));
        myRecognizer.setEventListener(chainRecogListener); // 替换掉原来的listener


    }

    private void initListener() {
        ((RadioGroup)findViewById(R.id.radio_tab)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_tab_1) {
                    setLanguage("1537");
                } else if (checkedId == R.id.rb_tab_2) {
                    setLanguage("1637");
                } else if (checkedId == R.id.rb_tab_3) {
                    setLanguage("1737");
                }
            }
        });
    }

    private void setLanguage(String s) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SpeechConstant.PID,s);
        editor.commit();
    }

    protected boolean running = false;

    protected void start() {

        // 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = fetchParams();


        // BaiduASRDigitalDialog的输入参数
        input = new DigitalDialogInput(myRecognizer, chainRecogListener, params);
        BaiduASRDigitalDialog.setInput(input); // 传递input信息，在BaiduASRDialog中读取,
        Intent intent = new Intent(this, BaiduASRDigitalDialog.class);

        running = true;
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        running = false;
        status = STATUS_NONE;
        if (requestCode == 2) {
            String message = "";
            if (resultCode == RESULT_OK) {
                ArrayList results = data.getStringArrayListExtra("results");
                if (results != null && results.size() > 0) {
                    message += results.get(0);
                }
                View view = getCurrentFocus();
                if (view instanceof EditText) {
                    ((EditText) view).setText(message);
                    getNextEdit(view).requestFocus();
                    synthesizer.speak("请输入下一个");
                }
            } else {
                message += "没有结果";
            }
            MyLogger.info(message);
        }

    }

    private EditText getNextEdit(View view) {
        if (view == etHeyue) {
            return etKekuan;
        } else if (view == etKekuan) {
            return etCommend;
        } else if (view == etCommend) {
            return etHeyue;
        }

        return etHeyue;
    }

    protected Map<String, Object> fetchParams() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //  上面的获取是为了生成下面的Map， 自己集成时可以忽略
        Map<String, Object> params = apiParams.fetch(sp);
        //  集成时不需要上面的代码，只需要params参数。
        return params;
    }

    /**
     * 开始录音后，手动点击“停止”按钮。
     * SDK会识别不会再识别停止后的录音。
     * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    protected void stop() {
        myRecognizer.stop();
    }


    /**
     * 开始录音后，手动点击“取消”按钮。
     * SDK会取消本次识别，回到原始状态。
     * 基于DEMO集成4.2 发送取消事件 取消本次识别
     */
    protected void cancel() {

        myRecognizer.cancel();
    }

    protected Handler mainHandler;

    protected MySyntherizer synthesizer;


    protected String appId;

    protected String appKey;

    protected String secretKey;

    protected String sn; // 纯离线合成SDK授权码；离在线合成SDK没有此参数

    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;

    protected InitConfig getInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = getParams();
        // 添加你自己的参数
        InitConfig initConfig;
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        if (sn == null) {
            initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        } else {
            initConfig = new InitConfig(appId, appKey, secretKey, sn, ttsMode, params, listener);
        }

        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                         Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });

        return initConfig;
    }


    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
        InitConfig config = getInitConfig(listener);
        synthesizer = new NonBlockSyntherizer(this, config, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
            // 在线SDK版本没有此参数。

            /*
            params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
            // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
            // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
            // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。
            */
            // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
            OfflineResource offlineResource = createOfflineResource(VOICE_FEMALE);
//            // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
        }
        return offlineResource;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synthesizer.release();
        myRecognizer.release();
    }
}
