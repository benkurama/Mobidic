package com.jivescribe.mt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.jivescribe.mt.objects.MobiDatabase;
import com.jivescribe.mt.objects.RecordDataObj;
import com.jivescribe.mt.objects.UserObj;
import com.jivescribe.mt.utils.Utils;

public class RecordListAct extends Activity implements OnItemClickListener,OnItemLongClickListener{
 // =========================================================================
 // TODO Variables
 // =========================================================================	
	private Button LogoutBtn;
	private ListView RecordViewLV;
	// ---------------------- //
	private MobiDatabase Database;
	// ---------------------- //
	private ArrayList<UserObj> UserInfo = new ArrayList<UserObj>();
	private ArrayList<RecordDataObj> RecordInfo = new ArrayList<RecordDataObj>();
	private ArrayList<HashMap<String,String>> RecordHash = new ArrayList<HashMap<String,String>>();
 // =========================================================================
 // TODO Activity Life Cycle
 // =========================================================================		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle sonicInstance) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(sonicInstance);
        setContentView(R.layout.act_record_list);
        
        SetupControls();
    }
    // =========================================================================
    @Override
	protected void onResume() {
		super.onResume();
		
		clearAndSetRecords();
	}
 // =========================================================================
 // TODO onClick View
 // =========================================================================
    public void onLogout(View v){
    	this.finish();
    }
 // =========================================================================
    public void onAddRec(View v){
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Record Filename");
    	// -----=-----=-----=----- ><
    	final EditText text = new EditText(this);
    	alert.setView(text);
    	
 		alert.setPositiveButton("Save", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Vars.setRecordName(text.getText().toString());
				callRecordPlay();
			}
		});
 		
 		alert.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});

 		alert.show(); 
    }
 // =========================================================================
 // TODO Implementation
 // =========================================================================
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
	
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Record Selection");

 		alert.setPositiveButton("View", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Vars.setPlaybackPos(pos);
				Vars.setRecordListObj(RecordInfo);
				gotoPlayback();
			}
		});
 		
 		alert.setNegativeButton("Edit", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Vars.setPlaybackPos(pos);
				Vars.setRecordListObj(RecordInfo);
				gotoEditRecord();
			}
		});

 		alert.show(); 
		
	}
	// =========================================================================
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Delete a Record ?");

 		alert.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				deleteRecord(pos);
				clearAndSetRecords();
			}
		});
 		
 		alert.setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});

 		alert.show(); 
 		
		return true;
	}
 // =========================================================================
 // TODO Functions
 // =========================================================================
    public void SetupControls(){
    	   	
    	UserInfo = Vars.getUserListObj();
    	
    	LogoutBtn = (Button)findViewById(R.id.btnLogout);
    	RecordViewLV = (ListView)findViewById(R.id.lvRecordView);
    	RecordViewLV.setOnItemClickListener(this);
    	RecordViewLV.setOnItemLongClickListener(this);
    }
 // =========================================================================
    public void callRecordPlay(){
    	
    	startActivity(new Intent(this,RecordPlayAct.class));
    }
    // =========================================================================
    public void getRecord(){
    	
    	Database = new MobiDatabase(this);
    	Database.openToRead();
    	RecordInfo = Database.getRecordQuery(UserInfo.get(0).ID);
    	Database.close();
    }
    // =========================================================================
    public void postRecords(){
    	
    	HashMap<String,String> RecordMap ;
    	
    	for(RecordDataObj RecordList: RecordInfo){
    		
    		RecordMap = new HashMap<String,String>();
    		RecordMap.put("Recordname", RecordList.RECORDNAME);
    		RecordMap.put("Duration", RecordList.DURATION);
    		RecordMap.put("Status", RecordList.STATUS);
    		RecordMap.put("Date", RecordList.DATE);
    		RecordMap.put("Priority", RecordList.PRIORITY);
    		
    		RecordHash.add(RecordMap);
    	}
    	
    	String[] RecordColumns = new String[]{"Recordname","Duration","Status","Date"};
    	int[] RecordControls = new int[]{R.id.tvRecordName,R.id.tvDuration,R.id.tvStatus,R.id.tvDate};
    	
    	ListAdapter Adapter = new SimpleAdapter(this,RecordHash,R.layout.records_row,RecordColumns,RecordControls);
    	RecordViewLV.setAdapter(Adapter);
    }
    // =========================================================================
    public void clearAndSetRecords(){
    	
    	RecordInfo.clear();
    	RecordHash.clear();
    	
    	getRecord();
    	
    	postRecords();
    }
    // =========================================================================
    public void deleteRecord(int pos){
    	
    	String path = RecordInfo.get(pos).RECORDPATH;
    	
    	File deleteRecord = new File(path);
    	boolean bol = deleteRecord.delete();
    	
    	if(bol){
    		Utils.MessageToast("Delete Success", this);
    	} else{
    		Utils.MessageToast("Delete Failed", this);
    	}
    	
    	Database = new MobiDatabase(this);
    	Database.openToWrite();
    	Database.deleteRecordQuery(RecordInfo.get(pos).ID);
    	Database.close();
    	
    }
    // =========================================================================
    public void gotoPlayback(){
    	startActivity(new Intent(this,PlaybackAct.class));
    }
    // =========================================================================
    public void gotoEditRecord(){
    	
    	startActivity(new Intent(this,EditRecordAct.class));
    }
 // =========================================================================
 // TODO Final Destination
}
