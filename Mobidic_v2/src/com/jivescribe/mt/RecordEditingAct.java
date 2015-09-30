package com.jivescribe.mt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.jivescribe.mt.objects.RecordDataObj;
import com.jivescribe.mt.utils.AudioEditor;
import com.jivescribe.mt.utils.RecordUtils;
import com.jivescribe.mt.utils.Utils;

public class RecordEditingAct extends Activity implements OnCompletionListener{
 // =========================================================================
 // TODO Variables
 // =========================================================================	
	
	private TextView RecordTitle,EditTitle;
	private Button RecPlayBtn,StopBtn,SaveBtn,CancelBtn;
	private Chronometer ChronoDur;
	// ---------------------- //
	private MediaRecorder recorder;
	private MediaPlayer player;
	private File audioFile,MainDirectory,File1,File2,File3,FileF;
	// ---------------------- //
	private long FirstDur,SecondDur,TotalDur;
	private boolean isRecord = false;
	// ---------------------- //
	private ArrayList<RecordDataObj> RecordInfo = new ArrayList<RecordDataObj>();
 // =========================================================================
 // TODO Activity Life Cycle
 // =========================================================================		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle sonicInstance) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(sonicInstance);
        setContentView(R.layout.act_record_editing);
        
        SetupControls();
        
        RecordTitle.setText("New Record");
    }
 // =========================================================================
 // TODO onClick View
 // =========================================================================
    public void onBack(View v){
    	this.finish();
    }
    // =========================================================================
    public void onRecPlay(View v){
    	 
    	RecordPlay();
    }
    // =========================================================================
    public void onStop(View v){
    	
    	Stop();
    }
    // =========================================================================
    public void onSave(View v){
    	
    	DuelCombi();
    }
    // =========================================================================
    public void onCancel(View v){
    	
    	File2.delete();
    	//FileF.delete();
    	backToCancel();
    }
 // =========================================================================
 // TODO Implementation
 // =========================================================================
    @Override
	public void onCompletion(MediaPlayer mp) {
		
    	playStop();
	}
 // =========================================================================
 // TODO Functions
 // =========================================================================
    public void SetupControls(){
    	
    	RecordInfo = Vars.getRecordlistObj();
    	
    	RecordTitle = (TextView)findViewById(R.id.tvRecordNameSub);
    	EditTitle = new TextView(this);
    	
    	RecPlayBtn = (Button)findViewById(R.id.btnRecordSub);
    	StopBtn = (Button)findViewById(R.id.btnStopSub);
    	SaveBtn = (Button)findViewById(R.id.btnSaveSub);
    	CancelBtn =  (Button)findViewById(R.id.btnCancelSub);
    	
    	ChronoDur = (Chronometer)findViewById(R.id.chrmDurationSub);
    	player = new MediaPlayer();
    	player.setOnCompletionListener(this);
    	
    	// -------------------------------------------------------------- Initialize ...
    	
    	StopBtn.setEnabled(false);
    	SaveBtn.setEnabled(false);
    	CancelBtn.setEnabled(false);
    	
    	Bundle getValue = getIntent().getExtras();
        
        MainDirectory = new File(getValue.getString("directory"));
        
        File1 = new File(getValue.getString("file1"));
        
        FirstDur = getValue.getLong("firstduration");
        TotalDur = getValue.getLong("totalduration");
        
        isRecord = true;
    }
    // =========================================================================
    public void RecordPlay(){
    	
    	if (isRecord){
    		
    		try {
   			 
   			 String recordname = RecordInfo.get(Vars.getPlaybackPos()).RECORDNAME;
   			 audioFile = File.createTempFile(recordname + "_", ".3gp", MainDirectory);
   			
   			 recorder = new MediaRecorder();
   			 recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
   			 recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
   			 recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
   			 recorder.setOutputFile(audioFile.getAbsolutePath());
   			 recorder.prepare();
   			 recorder.start();
   			
   			 ChronoDur.setBase(SystemClock.elapsedRealtime());
   			 ChronoDur.start();
   			 
   			RecPlayBtn.setEnabled(false);
   			StopBtn.setEnabled(true);
   			 
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
    		
    	} else {
    		
    		try {
    			
    			player.reset();
				player.setDataSource(audioFile.getAbsolutePath());
	    		player.prepare();
	    		player.start();
	    		
	    		RecPlayBtn.setEnabled(false);
	    		StopBtn.setEnabled(true);
	    		
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    	}
    }
    // =========================================================================
    public void Stop(){
    	
    	if (isRecord){
    		
    		recorder.stop();
    		recorder.release();
    		
    		ChronoDur.stop();
    		
    		RecPlayBtn.setEnabled(true);
    		RecPlayBtn.setText("Play");
    		StopBtn.setEnabled(false);
    		// - 
    		File2 = new File(audioFile.getAbsolutePath());
    		
    		isRecord = false;
    		
    	} else {
    		
    		playStop();
    	}
    }
    // =========================================================================
    public void DuelCombi(){
    	
    	//final File Start  = new File(RecordInfo.get(Vars.PlaybackPos).RECORDPATH);
    	final String AudioOne = RecordUtils.durationToFrames(FirstDur);
    	final String AudioTwo = RecordUtils.durationToFrames(SecondDur);
    	
    	final String lastFirst = RecordUtils.durationToFrames(FirstDur + SecondDur);
    	final String AudioThree = RecordUtils.durationToFrames(TotalDur);
    	
    	// --------------------------------------------------------------
		String mainDirectory = MainDirectory.getAbsolutePath();
		String recordname = RecordInfo.get(Vars.getPlaybackPos()).RECORDNAME;
		FileF = new File(mainDirectory,"/final_"+recordname+".3gp");
    	
    	final ProgressDialog dialog = ProgressDialog.show(this, "Please Wait..","Completing process...", true);
		final Handler handler = new Handler() {
		   public void handleMessage(Message msg) {
		      dialog.dismiss();
		      ///// 2nd if the load finish -----
		      backToMain();
			  ///// -----
		      }};
		      Thread checkUpdate = new Thread() {  
		   public void run() {	
			  /// 1st main activity here... -----
			  AudioEditor.combiAudio(File1,"0", AudioOne, File2,"0", AudioTwo, File1,lastFirst, AudioThree, FileF);
			  ////// -----
		      handler.sendEmptyMessage(0);		      
		      }};
		      checkUpdate.start();
    }
    // =========================================================================
    public void backToMain(){
    	
    	Bundle passValue = new Bundle();
    	Intent returnIntent = new Intent();
    	// --------------------------------------------------------------
    	passValue.putString("FinalFile", FileF.getAbsolutePath());
    	
    	returnIntent.putExtras(passValue);
    	
    	setResult(RESULT_OK,returnIntent);
    	
    	File2.delete();
    	this.finish();
    }
    // =========================================================================
    public void backToCancel(){
    	
    	Intent returnIntent = new Intent();
    	setResult(RESULT_CANCELED,returnIntent);
    	
    	this.finish();
    }
    // =========================================================================
    public void playStop(){
    	
    	SecondDur = player.getDuration();
		
		player.stop();
		player.release();
		
		StopBtn.setEnabled(false);
		
		SaveBtn.setEnabled(true);
		CancelBtn.setEnabled(true);
    }
 // =========================================================================
 // TODO Final Destination
}
