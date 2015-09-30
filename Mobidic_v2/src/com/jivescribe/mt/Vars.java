package com.jivescribe.mt;

import java.util.ArrayList;

import com.jivescribe.mt.objects.RecordDataObj;
import com.jivescribe.mt.objects.UserObj;
import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

public class Vars extends Application{

	public static ArrayList<UserObj> UserList;
	public static ArrayList<RecordDataObj> RecordList;
	public static String RecordName;
	public static Integer PlaybackPos;
	
	@Override
	public void onCreate() {
		super.onCreate();
		UserList = null;
		RecordList = null;
		RecordName = "";
		PlaybackPos = 0;
	}
// =========================================================================
// TODO Global Objects
// =========================================================================
	public static ArrayList<UserObj> getUserListObj() {
	    return UserList;
	}
	public static void setUserListObj(ArrayList<UserObj> userlist) {
		Vars.UserList = userlist;
	}
	public static void setUserListObjToNull() {
		Vars.UserList.clear();
	}
// =========================================================================	
	public static ArrayList<RecordDataObj> getRecordlistObj(){
		return RecordList;
	}
	public static void setRecordListObj(ArrayList<RecordDataObj> recordlist){
		Vars.RecordList = recordlist;
	}
	public static void setRecordListObjToNull(){
		Vars.RecordList = null;
	}
// ========================================================================= ><
	public static String getRecordName(){
		return RecordName;
	}
	public static void setRecordName(String record){
		Vars.RecordName = record;
	}
	// =========================================================================
	public static Integer getPlaybackPos(){
		return PlaybackPos;
	}
	public static void setPlaybackPos(Integer pos){
		Vars.PlaybackPos = pos;
	}
// =========================================================================
// TODO Preferences
// =========================================================================
	public static String getUsername(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getString("username", null);
	}
	public static void setUsername(Context context,String name){
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("username", name).commit();
	}
// =========================================================================
	public static String getPassword(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getString("password", null);
	}
	public static void setPassword(Context context,String pass){
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("password", pass).commit();
	}
// =========================================================================
// TODO Final Destination
}
