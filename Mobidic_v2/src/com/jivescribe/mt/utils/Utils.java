package com.jivescribe.mt.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	 // =========================================================================
	 // TODO Message Box | Page: Universal
	 // =========================================================================
		public static void MessageBox(String msg, Context context){
			
			AlertDialog.Builder alert = new AlertDialog.Builder(context);                 
	 		alert.setMessage(msg); 

	 		alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	 	    public void onClick(DialogInterface dialog, int which) {
	 	        //return;   
	 	    }});
	 		alert.show();
		}
		// =========================================================================
		public static void LogCat(String value){
			Log.i("MOBI",value);
		}
		// =========================================================================
		public static void MessageToast(String msg, Context core){
			
			Toast.makeText(core, msg, Toast.LENGTH_LONG);
		}
	 // =========================================================================
	 // TODO Final Destination
}
