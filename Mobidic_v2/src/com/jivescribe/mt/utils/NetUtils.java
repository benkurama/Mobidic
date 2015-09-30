package com.jivescribe.mt.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
	// =========================================================================
	public static boolean isNetworkOn(Context context) {
 		boolean haveConnectedWifi = false, haveConnectedMobile = false;
 		
 	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
 	    
 	    for (NetworkInfo ni : netInfo) {
 	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
 	            if (ni.isConnected())
 	                haveConnectedWifi = true;
 	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
 	            if (ni.isConnected())
 	                haveConnectedMobile = true;
 	    }
 	    return haveConnectedWifi || haveConnectedMobile;
 	}
 	 // =========================================================================
	public static HttpResponse connectionTimeout(String u){
		
		HttpResponse response = null;
		
		HttpGet httpGet = new HttpGet(u);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used. 
		int timeoutConnection = 10000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 10000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		try {
			
			response = httpClient.execute(httpGet);
			
		} catch (IOException e) {
			e.printStackTrace();
			response = null;
		}
	
		return response;
	}
	// =========================================================================
}
