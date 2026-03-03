package com.honeywell.barcodeexample;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.device.ScanDevice;
import android.hardware.BarcodeScan;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;



import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends Activity {
    private final static String SCAN_ACTION = "scan.rcv.message";
    private long exitTime;
    private WebView html_webView;
    private TextView processName;
    private LinearLayout progressBox;
    public static int version,serverVersion;
    public static String versionName,serverVersionName,downloadResult;
    private ProgressBar proBar;
    public static receiveVersionHandler handler;
    private UpdateManager updateManager = UpdateManager.getInstance();

    //alps机型
    ScanDevice sm;

    //C52机型
    private BarcodeScan mBarcodeScan;
    private MediaPlayer mmediaplayer;

    @JavascriptInterface
    public boolean sendVersionParams(final String Serverversion,final String downLoadUrl) {
        if(version<Integer.parseInt(Serverversion))
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateManager.compareVersion(MainActivity.this,Serverversion,"",downLoadUrl);
                }
            });
            return false;
        }
        else
        {
            return true;
        }


    }

    @JavascriptInterface
    public int GetLocalVersion() {
       return MainActivity.version;
    }

    //alps pda 监听方法
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            byte[] barocode = intent.getByteArrayExtra("barocode");
            int barocodelen = intent.getIntExtra("length", 0);
            byte temp = intent.getByteExtra("barcodeType", (byte) 0);
            byte[] aimid = intent.getByteArrayExtra("aimid");
            String value = new String(barocode, 0, barocodelen);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Barcode",value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //设置扫码回调
            html_webView.loadUrl("javascript:scanCode('"+jsonObject.toString()+"')");
        }
    };

    @SuppressLint("JavascriptInterface")
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proBar=(ProgressBar)findViewById(R.id.progressBar_id);

        Context c = this;
        version = updateManager.getVersion(c);
        versionName = updateManager.getVersionName(c);

        processName = (TextView) findViewById(R.id.progress_message);

        progressBox =(LinearLayout) findViewById(R.id.progressBox);
        progressBox.setVisibility(View.GONE);

//        textView = (TextView) findViewById(R.id.textview_id);
//        textView.setText("当前版本号:"+version+"\n"+"当前版本名:"+versionName);

        handler = new receiveVersionHandler();

        html_webView = this.findViewById(R.id.webview);
        html_webView.getSettings().setJavaScriptEnabled(true);  //启用javascript支持 用于访问页面中的javascript
        html_webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//允许js弹出窗口
        html_webView.getSettings().setDomStorageEnabled(true);
        html_webView.getSettings().setAllowFileAccessFromFileURLs(true);
        html_webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        html_webView.getSettings().setAllowFileAccess(true);    //设置在WebView内部是否允许访问文件
        html_webView.getSettings().setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        html_webView.getSettings().setAppCachePath(appCachePath);
        html_webView.getSettings().setAllowFileAccess(true);
        html_webView.getSettings().setAppCacheEnabled(true);
        html_webView.setWebViewClient(new WebViewClient());
        html_webView.setWebChromeClient(new WebChromeClient());
        html_webView.addJavascriptInterface(this, "mainobj");
        try {
            if (Build.VERSION.SDK_INT >= 16) {
                Class<?> clazz = html_webView.getSettings().getClass();
                Method method = clazz.getMethod(
                        "setAllowUniversalAccessFromFileURLs", boolean.class);//利用反射机制去修改设置对象
                if (method != null) {
                    method.invoke(html_webView.getSettings(), true);//修改设置
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        html_webView.loadUrl("file:///android_asset/login.html");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁用横屏

        //alps机型注册
//        sm = new ScanDevice();
//        sm.openScan();
//        sm.setOutScanMode(1);//启动就是0广播  1输入框模式

        //honeywell机型注册
//        AidcManager.create(this, new AidcManager.CreatedCallback() {
//            @Override
//            public void onCreated(AidcManager aidcManager) {
//                manager = aidcManager;
//
//                // get bar code instance from MainActivity
////                barcodeReader = MainActivity.getBarcodeObject();
//                try {
//
//                    barcodeReader = manager.createBarcodeReader();
//
//                    try {
//                            barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_128_ENABLED,true);
//                            barcodeReader.setProperty(BarcodeReader.PROPERTY_QR_CODE_ENABLED,true);
//                            barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
//                            barcodeReader.claim();//打开扫描功能
//                        } catch (UnsupportedPropertyException e) {
//                            Toast.makeText(MainActivity.this, "异常", Toast.LENGTH_SHORT).show();
//                        } catch (ScannerUnavailableException e) {
//                        Toast.makeText(MainActivity.this, "扫描异常", Toast.LENGTH_SHORT).show();
//                    }
//                    barcodeReader.addTriggerListener(MainActivity.this);
//                    barcodeReader.addBarcodeListener(MainActivity.this);
//                } catch (InvalidScannerNameException e) {
//                    Toast.makeText(MainActivity.this, "异常", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK && html_webView.canGoBack()){
            html_webView.goBack();
            return true;
        }else{
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                MainActivity.this.finish();
            }
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

//    static BarcodeReader getBarcodeObject() {
//        return barcodeReader;
//    }



//    @Override
//    public void onBarcodeEvent(final BarcodeReadEvent event) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                Toast.makeText(MainActivity.this, event.getBarcodeData(), Toast.LENGTH_SHORT).show();
//
////                // update UI to reflect the data
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("Barcode",event.getBarcodeData());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    jsonObject.put("Character",event.getCharset());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    jsonObject.put("CodeId",event.getCodeId());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    jsonObject.put("AIMId:",event.getAimId());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    jsonObject.put("Timestamp",event.getTimestamp());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                html_webView.loadUrl("javascript:scanCode('" + jsonObject.toString() + "')");
//            }
//        });
//    }



    @Override
    public void onResume() {
        super.onResume();

        //c52机型注册
//        mBarcodeScan = new BarcodeScan(this);  //初始化接口
//        IntentFilter scanDataIntentFilter = new IntentFilter();
//        scanDataIntentFilter.addAction("ACTION_BAR_SCAN");
//        registerReceiver(mScanDataReceiver, scanDataIntentFilter);
//
//        mmediaplayer = new MediaPlayer(); //初始化声音
//        mmediaplayer = MediaPlayer.create(this, R.raw.scanok);
//        mmediaplayer.setLooping(false);

        //alps机型注册
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);

//        //honeywell机型注册
//        if (barcodeReader != null) {
//            try {
//                barcodeReader.claim();
//            } catch (ScannerUnavailableException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    private BroadcastReceiver mScanDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals("ACTION_BAR_SCAN")) {
                String str = intent.getStringExtra("EXTRA_SCAN_DATA");
                if(str != null){
                    mmediaplayer.start();
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Barcode",str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //设置扫码回调
                html_webView.loadUrl("javascript:scanCode('"+jsonObject.toString()+"')");
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
//        if (barcodeReader != null) {
//            // release the scanner claim so we don't get any scanner
//            // notifications while paused.
//            barcodeReader.release();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sm != null) {
            sm.stopScan();
            sm.setScanLaserMode(8);
            sm.closeScan();
        }

//        if (barcodeReader != null) {
//            // unregister barcode event listener
//            barcodeReader.removeBarcodeListener(this);
//
//            // unregister trigger state change listener
//            barcodeReader.removeTriggerListener(this);
//        }
    }

    public class receiveVersionHandler extends Handler{
        @SuppressLint("WrongConstant")
        @Override
        public void handleMessage(Message msg) {
            proBar.setProgress(msg.arg1);
            progressBox.setVisibility(View.VISIBLE);
            processName.setText("正在下载更新："+msg.arg1+"%");
//            textView.setText("下载进度："+msg.arg1);
            if(msg.arg1 == 100){
                progressBox.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String path = Environment.getExternalStorageDirectory()+"/pwsUpdate.apk";
                intent.setDataAndType(Uri.fromFile(new File(path)),
                        "application/vnd.android.package-archive");

                startActivity(intent);
            }
        }

    }
}
