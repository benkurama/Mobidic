package com.jivescribe.mt;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jivescribe.mt.objects.RecordDataObj;
import com.jivescribe.mt.utils.TimeConvertion;

public class PlaybackAct extends Activity implements OnCompletionListener,SeekBar.OnSeekBarChangeListener{
 // =========================================================================
 // TODO Variables
 // =========================================================================	
	private TextView RecordNameTV,CurrentDuration,TotalDuration;
	private SeekBar seekPlayer;
	private Button PlayBtn,RewindBtn,ForwardBtn;
	// ---------------------- //
	private MediaPlayer player;
	private TimeConvertion TimeCon;
	private Handler mHandler = new Handler();
	// ---------------------- //
	private boolean oncePlayed = false;
	// =========================================================================
	private ArrayList<RecordDataObj> RecordInfo = new ArrayList<RecordDataObj>();
 // =========================================================================
 // TODO Activity Life Cycle
 // =========================================================================		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle sonicInstance) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(sonicInstance);
        setContentView(R.layout.act_playback);
        
        SetupControls();
        
        RecordNameTV.setText(RecordInfo.get(Vars.getPlaybackPos()).RECORDNAME);
        
        getDuration();
    }
  // =========================================================================
    @Override
	protected void onPause() {
		super.onPause();
		
		if(player.isPlaying()){
			player.stop();
		}
	}
 // =========================================================================
 // TODO onClick View
 // =========================================================================
    public void onBack(View v){
    	
    	this.finish();
    }
    // =========================================================================
    public void onPlay(View v){
    	
    	playStart();
    }
    // =========================================================================
    public void onRewind(View v){
    	
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
    public void onForward(View v){
    	
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
	 
	           TotalDuration.setText("" + TimeCon.milliSecondsToTimer(totalDuration));
	           CurrentDuration.setText("" + TimeCon.milliSecondsToTimer(currentDuration));
	 
	           int progress = (int)(TimeCon.getProgressPercentage(currentDuration, totalDuration));
	           seekPlayer.setProgress(progress);
	 
	           mHandler.postDelayed(this, 100);
       }
    };
    
    
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
		
		playStop();
	}
 // =========================================================================
 // TODO Functions
 // =========================================================================
    public void SetupControls(){
    	   	
    	RecordInfo = Vars.getRecordlistObj();
    	RecordNameTV = (TextView)findViewById(R.id.tvRecordNameP);
    	CurrentDuration = (TextView)findViewById(R.id.tvCurrentDurationP);
    	TotalDuration = (TextView)findViewById(R.id.tvTotalDurationP);
    	
    	player = new MediaPlayer();
    	player.setOnCompletionListener(this);
    	
    	TimeCon = new TimeConvertion();
    	
    	seekPlayer = (SeekBar)findViewById(R.id.sbSeekPlayerP);
		seekPlayer.setOnSeekBarChangeListener(this);
		seekPlayer.setEnabled(false);
		
		PlayBtn = (Button)findViewById(R.id.btnPlayP);
		RewindBtn = (Button)findViewById(R.id.btnRewindP);
		ForwardBtn = (Button)findViewById(R.id.btnForwardP);
		
		// -----=-----=-----=----- >< Initialize
		RewindBtn.setEnabled(false);
		ForwardBtn.setEnabled(false);
    }
    // =========================================================================
    public void playStart(){
    	
    	try {
    		
    		if(!oncePlayed){
    		
	    		seekPlayer.setProgress(0);
				seekPlayer.setMax(100);
				seekPlayer.setEnabled(true);
				
				// Updating progress bar
		        updateProgressBar();
	    		
		        playerInit();
				player.start();
				
				PlayBtn.setText("Pause");
				RewindBtn.setEnabled(true);
				ForwardBtn.setEnabled(true);
				
				oncePlayed = true;
    		} else {
    			
    			if(player.isPlaying()){
    				
    				player.pause();
    				PlayBtn.setText("Play");
    			}else{
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
    public void updateProgressBar() {
    	
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }
    // =========================================================================
    public void playerInit() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		
			player.reset();
			player.setDataSource(RecordInfo.get(Vars.getPlaybackPos()).RECORDPATH);
			player.prepare();
    }
    // =========================================================================
    public void getDuration(){
    	try {
    		
			playerInit();
			long totalDuration = player.getDuration();
			TotalDuration.setText("" + TimeCon.milliSecondsToTimer(totalDuration));
			 
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
    	
    	mHandler.removeCallbacks(mUpdateTimeTask);
    	
    	player.seekTo(0);
		player.stop();
		PlayBtn.setText("Play");
		seekPlayer.setEnabled(false);
		RewindBtn.setEnabled(false);
		ForwardBtn.setEnabled(false);
		
		oncePlayed = false;
    }
 // =========================================================================
 // TODO Final Destination
}