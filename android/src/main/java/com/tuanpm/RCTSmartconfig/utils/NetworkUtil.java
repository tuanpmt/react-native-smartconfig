//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.tuanpm.RCTSmartconfig.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetworkUtil {

	public static int NOT_CONNECTED = 0;
	public static int WIFI = 1;
	public static int MOBILE = 2;

	public static int getConnectionStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return WIFI;
			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return MOBILE;
		}
		return NOT_CONNECTED;
	}

	public static String getConnectionStatusString (Context context) {
		int connectionStatus = NetworkUtil.getConnectionStatus(context);
		if (connectionStatus == NetworkUtil.WIFI)
			return "Connected to Wifi";
		if (connectionStatus == NetworkUtil.MOBILE)
			return "Connected to Mobile Data";
		return "No internet connection";
	}

	public static String getWifiName (Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String wifiName = wifiManager.getConnectionInfo().getSSID();
		if (wifiName != null){
			if (!wifiName.contains("unknown ssid") && wifiName.length() > 2){
				if (wifiName.startsWith("\"") && wifiName.endsWith("\""))
					wifiName = wifiName.subSequence(1, wifiName.length() - 1).toString();
				return wifiName;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	public static String getGateway (Context context) {
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		System.out.println("NetWorkUtil--------------->getGatway:"+wm.getDhcpInfo().gateway);

		return NetworkUtil.intToIp(wm.getDhcpInfo().gateway);
	}

	public static String intToIp(int i) {
		String ip = (i & 0xFF) + "." +((i >> 8 ) & 0xFF) + "." +((i >> 16 ) & 0xFF) + "." +((i >> 24 ) & 0xFF );;
		System.out.println("NetWorkUtil--------------->intToIp:"+ip);

		return ip;
	}
}
