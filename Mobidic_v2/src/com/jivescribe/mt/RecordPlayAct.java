package com.jivescribe.mt;

import java.io.File;
import java.io.IOException;
import java.security.spec.EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jivescribe.mt.objects.MobiDatabase;
import com.jivescribe.mt.objects.UserObj;
import com.jivescribe.mt.utils.RecordUtils;
import com.jivescribe.mt.utils.TimeConvertion;
import com.jivescribe.mt.utils.Utils;

public class RecordPlayAct extends Activity implements OnCompletionListener,SeekBar.OnSeekBarChangeListener{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private TextView RecordTitleTV,CurrentDuration,TotalDuraton;
	private Button RecordBtn,PlayBtn,StopBtn,CancelBtn,SaveBtn;
	private Chronometer RecordTime;
	private SeekBar seekPlayer;
	// ---------------------- //
	private File MainDirectory,audioFile;
	private MediaRecorder recorder;
	private MediaPlayer player;
	private TimeConvertion TimeCon;
	private MobiDatabase Database;
	// ---------------------- //
	private Handler mHandler = new Handler();
	// ---------------------- //
	private Boolean isRecord = false;
	private Boolean isPlay = false;
	private Boolean oncePlayed = false;
	// ---------------------- //
	ArrayList<UserObj> UserInfos = new ArrayList<UserObj>();
 // =========================================================================
 // TODO Activity Life Cycle
 // =========================================================================	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle sonicInstance) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(sonicInstance);
        setContentView(R.layout.act_record_play);
        
        SetupControls();
        
        RecordUtils.setMaxVol(this);
        // -----=-----=-----=----- ><
        CreatDirectory();
        
        String recordName = Vars.getRecordName();
        RecordTitleTV.setText(recordName);
        
        PlayBtn.setEnabled(false);
        StopBtn.setEnabled(false);
        
    }
  // =========================================================================
 @Override
	protected void onPause() {
		super.onPause();
		
		if(player.isPlaying() && isPlay){
		
			player.stop();
		}
		
		if (isRecord){
			recordStop();
		}
		
	}
 // =========================================================================
 // TODO onClick View
 // =========================================================================
    public void onCancel(View v){
    	
    	if(audioFile == null){
    		
    	} else {
    		if (audioFile.exists()){
    			audioFile.delete();
    		}
    	}
    	
    	
    	this.finish();
    }
    // =========================================================================
    @SuppressLint("SimpleDateFormat")
	public void onSave(View v){
    	
    	if(!oncePlayed){
			try {
				
				player.reset();
				player.setDataSource(audioFile.getAbsolutePath());
				player.prepare();
				
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	// ------------- Step 1 >>> Convert Permanent file name from temporary
    	File newFile = new File(MainDirectory,Vars.getRecordName() + ".3gp");
    	audioFile.renameTo(newFile);
    	
    	String userID = UserInfos.get(0).ID+"";
    	String userName = UserInfos.get(0).NAME;
    	String recordName = Vars.getRecordName();
    	String status = "Pending";
    	String recordPath = newFile.getAbsolutePath();
    	
    	long totalDuration = player.getDuration();
    	String duration = TimeCon.milliSecondsToTimer(totalDuration)+"";
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy");
    	String date = sdf.format(new Date());
    	
    	String priority = "NonPriority";
    	String description = "Test Only File";
    	
    	Database = new MobiDatabase(this);
    	Database.openToWrite();
    	Database.insertRecordQuery(userID,userName,recordName,status,recordPath,duration,date,priority,description);
    	Database.close();
    	
    	this.finish();
    }
    // =========================================================================
    public void onRec(View v){
    	// ------------- Step 1 >>>
    	recordStart();
    	
    	RecordBtn.setEnabled(false);
    	StopBtn.setEnabled(true);
    	
    }
    // =========================================================================
    public void onStop(View v){
    	
    	if(isRecord){
	    	recordStop();
	    	
	    	StopBtn.setEnabled(false);
	    	RecordBtn.setEnabled(false);
	    	PlayBtn.setEnabled(true);
	    	isRecord = false;
    	}
    	
    	if(isPlay || player.isPlaying()){
    		
    		playStop();
    		
    	}
    }
    // =========================================================================
    public void onPlay(View v){
    	
    	playStart();
    	
    }
    // =========================================================================
    public void onRewind (View v){
    	
    	int seekBackward = (player.getDuration() / 20);
    	
    	int currentPosition = player.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if(currentPosition - seekBackward >= 0){
            // backward song
        	player.seekTo(currentPosition - seekBackward);
        }else{
            // backward to starting position
        	player.seekTo(0);
        }
    }
    // =========================================================================
    public void onForward (View v){
    	
    	int seekForward = (player.getDuration() / 20);
    	
        int currentPosition = player.getCurrentPosition();
        // check if seekForward time is lesser than song duration
        if(currentPosition + seekForward <= player.getDuration()){
            // forward song
        	player.seekTo(currentPosition + seekForward);
        }else{
            // forward to end position
        	player.seekTo(player.getDuration());
        }
    }
 // =========================================================================
 // TODO Implementation
 // =========================================================================
    private Runnable mUpdateTimeTask = new Runnable() {
	// TODO Runnable
           public void run() {
	           long totalDuration = player.getDuration();
	           long currentDuration = player.getCurrentPosition();
//	 
	           TotalDuraton.setText("" + TimeCon.milliSecondsToTimer(totalDuration));
	           CurrentDuration.setText("" + TimeCon.milliSecondsToTimer(currentDuration));
//	 
	           int progress = (int)(TimeCon.getProgressPercentage(currentDuration, totalDuration));
	           seekPlayer.setProgress(progress);
	 
	           mHandler.postDelayed(this, 100);
       }
    };
    // =========================================================================
    @Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
		mHandler.removeCallbacks(mUpdateTimeTask);
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
		mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = player.getDuration();
        int currentPosition = TimeCon.progressToTimer(seekBar.getProgress(), totalDuration);
 
        // forward or backward to certain seconds
        player.seekTo(currentPosition);
 
        // update timer progress again
        updateProgressBar();
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		
		mHandler.removeCallbacks(mUpdateTimeTask);
		playStop();
	}
 // =========================================================================
 // TODO Functions
 // =========================================================================
    public void SetupControls(){
    	   	
    	UserInfos = Vars.getUserListObj();
    	
    	RecordTitleTV = (TextView)findViewById(R.id.tvRecordTitle);
    	CurrentDuration = (TextView)findViewById(R.id.tvCurrentDuration);
    	TotalDuraton = (TextView)findViewById(R.id.tvTotalDuration);
    	
    	RecordBtn = (Button)findViewById(R.id.btnRecord);
    	PlayBtn = (Button)findViewById(R.id.btnPlay);
    	StopBtn = (Button)findViewById(R.id.btnStop);
    	CancelBtn = (Button)findViewById(R.id.btnCancel);
    	SaveBtn = (Button)findViewById(R.id.btnSave);
    	
    	player = new MediaPlayer();
    	player.setOnCompletionListener(this);
    	
    	RecordTime = (Chronometer)findViewById(R.id.chrmTime);
    	
    	seekPlayer = (SeekBar)findViewById(R.id.sbProgress);
		seekPlayer.setOnSeekBarChangeListener(this);
		seekPlayer.setEnabled(false);
		
		TimeCon = new TimeConvertion();
		
		
    }
    // =========================================================================
    public void CreatDirectory(){
    	// ------------- Step 1 >>>
        String filePath = "/MobidicAudio/";
        MainDirectory = new File(Environment.getExternalStorageDirectory(), filePath);
        
        if(!MainDirectory.exists()){
        	MainDirectory.mkdir();
        }
        // ------------- Step 2 >>>
        filePath += "/"+UserInfos.get(0).USERNAME+"/";
        MainDirectory = new File(Environment.getExternalStorageDirectory(), filePath);
        
        if(!MainDirectory.exists()){
        	MainDirectory.mkdir();
        }
    }
    // =========================================================================
    public void recordStart(){
    
    	try {
    		
    		String fileName = Vars.getRecordName();
			audioFile = File.createTempFile(fileName, ".3gp", MainDirectory);
			
			// ------------- Step 2 >>>
	    	recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(audioFile.getAbsolutePath());
			recorder.prepare();
			recorder.start();
			
			RecordTime.setBase(SystemClock.elapsedRealtime());
	    	RecordTime.start();
	    	
	    	isRecord = true;
	    	seekPlayer.setEnabled(false);
	    	CancelBtn.setEnabled(false);
			
		} catch (IOException e) {
			
			Utils.MessageToast("SD card Access Error", this);
			e.printStackTrace();
		}
    }
    // =========================================================================
    public void recordStop(){
    	
    	recorder.stop();
		recorder.release();
		
    	RecordTime.stop();
    	RecordTime.setText("00:00");
    	RecordBtn.setEnabled(false);
    	CancelBtn.setEnabled(true);
    }
    // =========================================================================
    public void playStart(){
    	
    	try {
    		
    		if(!oncePlayed){
    		
	    		//File path = new File(Environment.getExternalStorageDirectory(),"/Benkurama/say.mp3");
	    		
	    		seekPlayer.setProgress(0);
				seekPlayer.setMax(100);
				seekPlayer.setEnabled(true);
				
				// Updating progress bar
		        updateProgressBar();
		        
	    		player.reset();
				player.setDataSource(audioFile.getAbsolutePath());
				//player.setDataSource(path.getAbsolutePath());
				player.prepare();
				player.start();
				
				isPlay = true;
				oncePlayed = true;
				
				PlayBtn.setText("Pause");
				//RecordBtn.setEnabled(false);
				StopBtn.setEnabled(true);
    		} else {
    			
    			if(player.isPlaying()){
    				
    				player.pause();
        			PlayBtn.setText("Play");
    			} else {
    				player.start();
    				PlayBtn.setText("Pause");
    			}
    			
    		}
			
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
    // =========================================================================
    public void playStop(){
    	
    	player.stop();
		isPlay = false;
		PlayBtn.setText("Play");
		
		seekPlayer.setEnabled(false);
		oncePlayed = false;
		//RecordBtn.setEnabled(true);
		StopBtn.setEnabled(false);
    }
    // =========================================================================
    public void updateProgressBar() {
    	
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }
 // =========================================================================
 // TODO Final Destination
}
