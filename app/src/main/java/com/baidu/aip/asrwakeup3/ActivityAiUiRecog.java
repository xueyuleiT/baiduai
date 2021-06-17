package com.baidu.aip.asrwakeup3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.controller.MySyntherizer;
import com.baidu.aip.asrwakeup3.controller.NonBlockSyntherizer;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.mini.InitConfig;
import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.listener.UiMessageListener;
import com.baidu.aip.asrwakeup3.uiasr.params.CommonRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.NluRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OfflineRecogParams;
import com.baidu.aip.asrwakeup3.util.Auth;
import com.baidu.aip.asrwakeup3.util.ChineseNumberUtil;
import com.baidu.aip.asrwakeup3.util.IOfflineResourceConst;
import com.baidu.aip.asrwakeup3.util.OfflineResource;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.baidu.aip.asrwakeup3.util.IOfflineResourceConst.VOICE_FEMALE;


public class ActivityAiUiRecog extends ActivityAi implements IStatus {

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;


    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    protected boolean enableOffline = true;
    /*
     * Api的参数类，仅仅用于生成调用START的json字符串，本身与SDK的调用无关
     */
    private  CommonRecogParams apiParams;

    /**
     * 控制UI按钮的状态
     */
    protected int status;

    /**
     * 日志使用
     */
    private static final String TAG = "ActivityUiRecog";




    protected Map<String, Object> fetchParams() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //  上面的获取是为了生成下面的Map， 自己集成时可以忽略
        Map<String, Object> params = apiParams.fetch(sp);
        //  集成时不需要上面的代码，只需要params参数。
        return params;
    }

    int index = 0;

    private List<UiData> uiDataList = new ArrayList<>();

    class UiData {

        UiData(EditText edit,String speakStr) {
            this.edit = edit;
            this.speakStr = speakStr;
        }

        UiData(EditText edit,String speakStr,Boolean isNumber) {
            this.edit = edit;
            this.speakStr = speakStr;
            this.isNumber = isNumber;
        }

        UiData(EditText edit,String speakStr,String speakStr2) {
            this.edit = edit;
            pair = new Pair<>(speakStr,speakStr2);
        }

        boolean isNumber = true;
        Pair<String,String> pair;
        EditText edit;
        String speakStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        etHeyue.requestFocus();


        uiDataList.add(new UiData(etHeyue,"合约号",false));
        uiDataList.add(new UiData(etKekuan,"客款号",false));
        uiDataList.add(new UiData(etCommend,"备注,",false));


        uiDataList.add(new UiData((EditText) findViewById(R.id.a1m),"M码,身长,领边度下"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.b1m),"胸阔,夹衣度"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.c1m),"膊阔,缝至缝"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.d1m),"锈长,膊顶度"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.e1m),"拉链长"));

        uiDataList.add(new UiData((EditText) findViewById(R.id.a1l),"L码,身长,领边度下"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.b1l),"胸阔,夹衣度"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.c1l),"膊阔,缝至缝"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.d1l),"锈长,膊顶度"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.e1l),"拉链长"));

        uiDataList.add(new UiData((EditText) findViewById(R.id.a1xl),"XL码,身长,领边度下"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.b1xl),"胸阔,夹衣度"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.c1xl),"膊阔,缝至缝"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.d1xl),"锈长,膊顶度"));
        uiDataList.add(new UiData((EditText) findViewById(R.id.e1xl),"拉链长"));


        for (final UiData uiData: uiDataList) {
            uiData.edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (status == STATUS_NONE) {
                        return;
                    }

                    if (hasFocus) {
                        synthesizer.stop();

                        stop();
                        status = STATUS_NONE; // 引擎识别中
                        btnStart.setEnabled(false);

                        if (uiData.pair != null) {

                            List<Pair<String, String>> list = new ArrayList<>();
                            list.add(uiData.pair);
                            synthesizer.batchSpeak(list);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    start();
                                    status = STATUS_WAITING_READY;
                                    btnStart.setEnabled(true);
                                }
                            },3000);

                        } else {

                            synthesizer.speak(uiData.speakStr);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    start();
                                    status = STATUS_WAITING_READY;
                                    btnStart.setEnabled(true);
                                }
                            },(uiData.speakStr.length() / 3) * 2000 );
                        }
                    }
                }
            });
        }



        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String PID = sp.getString(SpeechConstant.PID,"1537");

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SpeechConstant.VAD_ENDPOINT_TIMEOUT,"0");
        editor.commit();

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
                XXPermissions.with(ActivityAiUiRecog.this)
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
                                            btnStart.setText("停止录音");
                                            btnStart.setEnabled(false);
                                            synthesizer.speak(uiDataList.get(getIndex(getCurrentFocus())).speakStr);
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    start();
                                                    status = STATUS_WAITING_READY;
                                                    updateBtnTextByStatus();
                                                }
                                            },(uiDataList.get(getIndex(getCurrentFocus())).speakStr.length() / 3) * 2000);
                                            break;
                                        case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                                        case STATUS_READY: // 引擎准备完毕。
                                        case STATUS_SPEAKING: // 用户开始讲话
                                        case STATUS_FINISHED: // 一句话识别语音结束
                                        case STATUS_RECOGNITION: // 识别中
                                        case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                                        case STATUS_STOPPED:
                                            stop();
                                            status = STATUS_NONE; // 引擎识别中
                                            updateBtnTextByStatus();
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

    private ChainRecogListener chainRecogListener;

    protected Handler mainHandler;

    protected MySyntherizer synthesizer;

    protected String appId;

    protected String appKey;

    protected String secretKey;

    protected String sn; // 纯离线合成SDK授权码；离在线合成SDK没有此参数

    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;


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

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(VOICE_FEMALE);
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        return params;
    }

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
                Log.d(TAG  + "1",msg.toString());
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

    private void init() {
        Auth.getInstance(this);

        apiParams = new NluRecogParams();

        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG + "2",msg.toString());
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
                Log.d(TAG  + "4",msg.toString());
                if (msg.what ==  4) {
                }
            }

        });

        handler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d(TAG  + "6",msg.toString());
               if (msg.what == 6 && msg.arg1 == 6) {
                   if (msg.arg2 == 1) {
                       View view = getCurrentFocus();
                       if (view instanceof EditText) {
                           String result = msg.obj.toString().split("；")[0];
                           if (result.contains("asr.finish")) {
                               return;
                           }

                           int index = getIndex(view);
                           if (result.contains("上一个") || result.contains("上一格")
                                   || result.contains("上一项")
                                   || result.contains("上一条")
                                   || result.contains("前一个")
                                   || result.contains("前一格")
                                   || result.contains("前一项")
                                   || result.contains("前一条")
                                   || result.contains("后退")) {
                               if (index == 0) {
                                   speak("已经到头了",2000);
                                   return;
                               }

                               uiDataList.get(index - 1).edit.requestFocus();
                               return;
                           }

                           if (result.contains("下一个") || result.contains("下一格")
                                   || result.contains("下一项")
                                   || result.contains("下一条")
                                   || result.contains("后一个")
                                   || result.contains("后一格")
                                   || result.contains("后一项")
                                   || result.contains("后一条")
                                   || result.contains("前进")) {
                               if (index == uiDataList.size() - 1) {
                                   speak("已经到尾了",2000);
                                   return;
                               }

                               uiDataList.get(index + 1).edit.requestFocus();
                               return;
                           }

                           result = result.replaceAll("。", "");

                           if (uiDataList.get(index).isNumber) {
                               result = ChineseNumberUtil.convertString(result.replaceAll("。", ""));
                               if (!ChineseNumberUtil.isNumber(result)) {
                                   speak("输入错误，请重新输入", 3000);
                                   return;
                               }
                           }

                           ((EditText) view).setText(result);

                           if (index == uiDataList.size() - 1) {
                               index = 0;
                           } else {
                               index ++;
                           }
                           uiDataList.get(index).edit.requestFocus();
                       }
                   }
               }
            }
        };

        myRecognizer = new MyRecognizer(this, listener);

        if (enableOffline) {
            // 基于DEMO集成1.4 加载离线资源步骤(离线时使用)。offlineParams是固定值，复制到您的代码里即可
            Map<String, Object> offlineParams = OfflineRecogParams.fetchOfflineParams();
            myRecognizer.loadOfflineEngine(offlineParams);
        }

        chainRecogListener = new ChainRecogListener();
        chainRecogListener.addListener(new MessageStatusRecogListener(handler));
        myRecognizer.setEventListener(chainRecogListener); // 替换掉原来的listener


    }

    private void speak(String s,long time) {
        stop();
        status = STATUS_NONE; // 引擎识别中
        btnStart.setEnabled(false);

        synthesizer.speak(s);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
                status = STATUS_WAITING_READY;
                btnStart.setEnabled(true);
            }
        },time);
    }

    private int getIndex(View view) {

        for (int i = 0; i < uiDataList.size(); i ++) {
            if (view == uiDataList.get(i).edit) {
                return i;
            }
        }

        return 0;
    }


    private void setLanguage(String s) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SpeechConstant.PID,s);
        editor.commit();
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

    @Override
    protected void initView() {
        super.initView();
        status = STATUS_NONE;
//        btnStart.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                switch (status) {
//                    case STATUS_NONE: // 初始状态
//
//                        synthesizer.speak(uiDataList.get(getIndex(getCurrentFocus())).speakStr);
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                start();
//                                status = STATUS_WAITING_READY;
//                                updateBtnTextByStatus();
//                            }
//                        },2000);
//                        break;
//                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
//                    case STATUS_READY: // 引擎准备完毕。
//                    case STATUS_SPEAKING: // 用户开始讲话
//                    case STATUS_FINISHED: // 一句话识别语音结束
//                    case STATUS_RECOGNITION: // 识别中
//                        stop();
//                        status = STATUS_STOPPED; // 引擎识别中
//                        updateBtnTextByStatus();
//                        break;
//                    case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
//                    case STATUS_STOPPED: // 引擎识别中
//                        cancel();
//                        status = STATUS_NONE; // 识别结束，回到初始状态
//                        updateBtnTextByStatus();
//                        break;
//                    default:
//                        break;
//                }
//
//            }
//        });
    }

    protected void handleMsg(Message msg) {
        super.handleMsg(msg);

        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                status = msg.what;
                updateBtnTextByStatus();
                break;
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;
                updateBtnTextByStatus();
                break;
            default:
                break;

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

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
            case STATUS_LONG_SPEECH_FINISHED:
            case STATUS_STOPPED:
                btnStart.setText("开始");
                btnStart.setEnabled(true);
                break;
            case STATUS_WAITING_READY:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                btnStart.setText("停止录音");
                btnStart.setEnabled(true);
                break;
            //                btn.setText("取消整个识别过程");
//                btn.setEnabled(true);
//                setting.setEnabled(false);
            default:
                break;
        }
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    protected void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = fetchParams();
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                Log.d(TAG  + "7",msg.toString());
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                    }
                }
            }
        }, enableOffline)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
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

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 如果之前调用过myRecognizer.loadOfflineEngine()， release()里会自动调用释放离线资源
        // 基于DEMO5.1 卸载离线资源(离线时使用) release()方法中封装了卸载离线资源的过程
        // 基于DEMO的5.2 退出事件管理器
        myRecognizer.release();
        synthesizer.release();
        Log.i(TAG, "onDestory");

        // BluetoothUtil.destory(this); // 蓝牙关闭

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
        status = STATUS_NONE; // 引擎识别中
        btnStart.setEnabled(true);

    }
}
