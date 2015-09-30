package com.jivescribe.mt.objects;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MobiDatabase {
 // =========================================================================
 // TODO Variables
 // =========================================================================
	public static String MY_DATABASE_NAME = "MobiDatabase";
	public static int MY_DATABASE_VERSION = 1;

	public SQLiteDatabase sqLiteDatabase;
	public SQLiteHelper sqLiteHelper;
	public Context context;
 // =========================================================================
 // TODO Class Object for SQLiteHelper
 // =========================================================================
	public class SQLiteHelper extends SQLiteOpenHelper {
		public SQLiteHelper(Context context, String name,CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
 // =========================================================================
 // TODO Activity Life Cycle
 // =========================================================================
	@Override
	public void onCreate(SQLiteDatabase db) {
		//Login Table
//		db.execSQL("create table Login (id integer primary key autoincrement,username text,password text,name text,email text)");
//		db.execSQL("insert into Login (username,password,name,email) values ('benkurama','pass123','Alvin','benkurama@gmail.com')");
//		db.execSQL("insert into Login (username,password,name,email) values ('admin','asdf','Admin','benkurama@gmail.com')");
		//Record Table
		db.execSQL("create table Record (id integer primary key autoincrement,userid integer,name text,recordname text,status text,recordpath text,duration text,date text,priority text,description text)");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}	
 // =========================================================================
 // TODO Main Functions
 // =========================================================================	
	public MobiDatabase(Context c){
		context = c;
	}
	/// Default Functions
	public MobiDatabase openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, MY_DATABASE_NAME, null, MY_DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this;	
	}
	public MobiDatabase openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, MY_DATABASE_NAME, null,MY_DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getWritableDatabase();
		return this;	
	}
	public void close(){
		sqLiteHelper.close();
	}
 // =========================================================================
 // TODO Implementation
 // =========================================================================
	public void insertRecordQuery(String userid,String username,String recordname,String status,String recordpath,String duration,String date,String priority,String description){
		
		ContentValues addRecords = new ContentValues();
		
		addRecords.put("userid", userid);
		addRecords.put("name", username);
		addRecords.put("recordname", recordname);
		addRecords.put("status", status);
		addRecords.put("recordpath", recordpath);
		addRecords.put("duration", duration);
		addRecords.put("date", date);
		addRecords.put("priority", priority);
		addRecords.put("description", description);
		
		sqLiteDatabase.insert("Record", null, addRecords);
	}
	// =========================================================================
	public ArrayList<RecordDataObj> getRecordQuery(int userid){
		
		ArrayList<RecordDataObj> RecordList = new ArrayList<RecordDataObj>();
		
		Cursor cur = sqLiteDatabase.rawQuery("select * from Record where userid=" + userid + "", null);
		
		if(cur.getCount() != 0){
			
			for(cur.moveToFirst();!(cur.isAfterLast());cur.moveToNext()){
				
				RecordDataObj record = new RecordDataObj();
				
				record.ID = cur.getInt(0);
				record.USERID = cur.getInt(1);
				record.USERNAME = cur.getString(2);
				record.RECORDNAME = cur.getString(3);
				record.STATUS = cur.getString(4);
				record.RECORDPATH = cur.getString(5);
				record.DURATION = cur.getString(6);
				record.DATE = cur.getString(7);
				record.PRIORITY = cur.getString(8);
				record.DESCRIPTION = cur.getString(9);
				
				RecordList.add(record);
			}
		}
		
		return RecordList; 
	}
	// =========================================================================
	public void deleteRecordQuery(int id){
		
		sqLiteDatabase.delete("Record where id = "+id+"", null, null);
	}
 // =========================================================================
 // TODO Final Destination
}
