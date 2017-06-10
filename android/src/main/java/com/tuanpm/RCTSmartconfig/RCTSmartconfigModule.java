/**
 * Created by TuanPM (tuanpm@live.com) on 1/4/16.
 */

package com.tuanpm.RCTSmartconfig;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.react.bridge.*;

import javax.annotation.Nullable;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.integrity_project.smartconfiglib.SmartConfig;
import com.integrity_project.smartconfiglib.SmartConfigListener;
import com.tuanpm.RCTSmartconfig.UdpReceive.ConfigSuccessListener;
import com.tuanpm.RCTSmartconfig.utils.MDnsCallbackInterface;
import com.tuanpm.RCTSmartconfig.utils.MDnsHelper;
import com.tuanpm.RCTSmartconfig.utils.NetworkUtil;
import com.tuanpm.RCTSmartconfig.utils.SmartConfigConstants;

public class RCTSmartconfigModule extends ReactContextBaseJavaModule {

    private static final String TAG = "RCTSmartconfigModule";

    private final ReactApplicationContext _reactContext;

    private IEsptouchTask mEsptouchTask;

    public RCTSmartconfigModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;

    }

    @Override
    public String getName() {
        return "Smartconfig";
    }

    @ReactMethod
    public void stop(type) {
        if (type == "cc3000"){
            this.stopCC3000()
        }else{
            if (mEsptouchTask != null) {
                Log.d(TAG, "cancel task");
                mEsptouchTask.interrupt();
            }
        }

    }

    @ReactMethod
    public void start(final ReadableMap options, final Promise promise) {
        String type = options.getString("type");
        String pass = options.getString("password");
        if (type == "cc3000"){
            return this.startCC3000(options, promise);
        }else{
            return this.startEsptouch(options, promise);
        }

    }
    private void stopConfig() {
        btn.setText(getString(R.string.canceling));
        btn.setEnabled(false);
        if(udpReceive != null){
            udpReceive.stopReceive();
            udpReceive = null;
        }
        new Thread() {
            public void run() {
                try {
                    smartConfig.stopTransmitting();
                    mDnsHelper.stopDiscovery();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
    private void startCC3000(final ReadableMap options, final Promise promise) {
        this.startCC3000SmartConfig(options);
    }
    private void stopCC3000() {
        new Thread() {
            public void run() {
                try {
                    smartConfig.stopTransmitting();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void startCC3000SmartConfig(options) {
        String passwordKey = options.getString("password").trim();
        byte[] paddedEncryptionKey;
        String SSID = options.getString("ssid").trim();
        String gateway = NetworkUtil.getGateway(this);
        paddedEncryptionKey = null;

        freeData = new byte[1];
        freeData[0] = 0x03;
        smartConfig = null;
        smartConfigListener = new SmartConfigListener() {
            @Override
            public void onSmartConfigEvent(SmtCfgEvent event, Exception e) {
               System.out.println("onSmartConfigEvent----------->"+event.name()+" toString:"+event.toString());
            }
        };
        try {
            smartConfig = new SmartConfig(smartConfigListener, freeData,
                    passwordKey, paddedEncryptionKey, gateway, SSID, (byte) 0,
                    "");
            smartConfig.transmitSettings();

            contentTotal = "";
            deviceCount = 0;

            scanForDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startEsptouch(final ReadableMap options, final Promise promise) {
        String ssid = options.getString("ssid");
        String pass = options.getString("password");
        Boolean hidden = false;
        //Int taskResultCountStr = 1;
        Log.d(TAG, "ssid " + ssid + ":pass " + pass);
        stop();
        new EsptouchAsyncTask(new TaskListener() {
            @Override
            public void onFinished(List<IEsptouchResult> result) {
                // Do Something after the task has finished

                WritableArray ret = Arguments.createArray();

                Boolean resolved = false;
                for (IEsptouchResult resultInList : result) {
                    if(!resultInList.isCancelled() &&resultInList.getBssid() != null) {
                        WritableMap map = Arguments.createMap();
                        map.putString("bssid", resultInList.getBssid());
                        map.putString("ipv4", resultInList.getInetAddress().getHostAddress());
                        ret.pushMap(map);
                        resolved = true;
                        if (!resultInList.isSuc())
                            break;

                    }
                }

                if(resolved) {
                    Log.d(TAG, "Success run smartconfig");
                    promise.resolve(ret);
                } else {
                    Log.d(TAG, "Error run smartconfig");
                    promise.reject("new IllegalViewOperationException()");
                }

            }
        }).execute(ssid, new String(""), pass, "YES", "1");
        //promise.resolve(encoded);
        //promise.reject("Error creating media file.");
        //
        //Toast.makeText(getReactApplicationContext(), ssid + ":" + pass, 10).show();
    }

    public interface TaskListener {
        public void onFinished(List<IEsptouchResult> result);
    }

    private class EsptouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {

      //
      // public interface TaskListener {
      //     public void onFinished(List<IEsptouchResult> result);
      // }
      private final TaskListener taskListener;

      public EsptouchAsyncTask(TaskListener listener) {
        // The listener reference is passed in through the constructor
        this.taskListener = listener;
      }


      // without the lock, if the user tap confirm and cancel quickly enough,
      // the bug will arise. the reason is follows:
      // 0. task is starting created, but not finished
      // 1. the task is cancel for the task hasn't been created, it do nothing
      // 2. task is created
      // 3. Oops, the task should be cancelled, but it is running
      private final Object mLock = new Object();

      @Override
      protected void onPreExecute() {
        Log.d(TAG, "Begin task");
      }
      @Override
      protected List<IEsptouchResult> doInBackground(String... params) {
        Log.d(TAG, "doing task");
        int taskResultCount = -1;
        synchronized (mLock) {
          String apSsid = params[0];
          String apBssid =  params[1];
          String apPassword = params[2];
          String isSsidHiddenStr = params[3];
          String taskResultCountStr = params[4];
          boolean isSsidHidden = false;
          if (isSsidHiddenStr.equals("YES")) {
            isSsidHidden = true;
          }
          taskResultCount = Integer.parseInt(taskResultCountStr);
          mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
              isSsidHidden, getCurrentActivity());

          //mEsptouchTask.setEsptouchListener(myListener);
        }
        List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
        return resultList;
      }

      @Override
      protected void onPostExecute(List<IEsptouchResult> result) {

        IEsptouchResult firstResult = result.get(0);
        // check whether the task is cancelled and no results received
        if (!firstResult.isCancelled()) {
          if(this.taskListener != null) {

           // And if it is we call the callback function on it.
           this.taskListener.onFinished(result);
         }
        }
      }
    }
}
