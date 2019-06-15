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
import com.espressif.iot.esptouch.util.EspAES;
import com.espressif.iot.esptouch.task.__IEsptouchTask;

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
    public void stop() {
      if (mEsptouchTask != null) {
        Log.d(TAG, "cancel task");
        mEsptouchTask.interrupt();
      }
    }

    @ReactMethod
    public void start(final ReadableMap options, final Promise promise) {
      String ssid = options.getString("ssid");
      String bssid = options.getString("bssid");
      String pass = options.getString("password");
      int taskCount = options.getInt("taskCount");
      Boolean hidden = false;
      Log.d(TAG, "ssid " + ssid + ":pass " + pass);
      stop();
      new EsptouchAsyncTask(new TaskListener() {
        @Override
        public void onFinished(List<IEsptouchResult> result) {
            // Do Something after the task has finished

            WritableArray ret = Arguments.createArray();

            Boolean resolved = false;
            try{
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
              promise.reject("Timeoutout");
            }
            }catch(Exception e){
               Log.d(TAG, "Error, Smartconfig could not complete!");
               promise.reject("Timeoutout", e);
            }
        }
      }).execute(ssid, bssid, pass, Integer.toString(taskCount));
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
        int taskCount = -1;
        synchronized (mLock) {
          String apSsidB64 = params[0];
          byte[] apSsid = Base64.decode(apSsidB64, Base64.DEFAULT);
          String apBssid64 =  params[1];
          byte[] apBssid = Base64.decode(apBssid64, Base64.DEFAULT);
          String apPasswordB64 = params[2];
          byte[] apPassword = Base64.decode(apPasswordB64, Base64.DEFAULT);
          Log.d(TAG, apSsid + " | " + apBssid + " | " + apPassword);
          String taskCountStr = params[3]; 
          taskCount = Integer.parseInt(taskCountStr); 
          mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, getCurrentActivity());
        }
        List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskCount);
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
