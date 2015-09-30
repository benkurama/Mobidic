package com.jivescribe.mt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jivescribe.mt.objects.AudioEditorAsync;
import com.jivescribe.mt.objects.RecordDataObj;
import com.jivescribe.mt.objects.UserObj;
import com.jivescribe.mt.utils.AudioEditor;
import com.jivescribe.mt.utils.RecordUtils;
import com.jivescribe.mt.utils.TimeConvertion;
import com.jivescribe.mt.utils.Utils;

public class EditRecordAct extends Activity implements OnCompletionListener,SeekBar.OnSeekBarChangeListener {
	// =========================================================================
	// TODO Variables
	// =========================================================================
	private TextView TotalDuration, CurrentDuration;
	private SeekBar SeekPlayer;
	private Button RecordBtn, PlayBtn, StopBtn, SaveBtn;
	// ---------------------- //
	private MediaPlayer player;
	private TimeConvertion TimeCon;
	private Handler mHandler = new Handler();
	private File MainDirectory;
	// ---------------------- //
	private File FileF;
	private long firstDur, TotalDur;
	// ---------------------- //
	private boolean oncePlayed = false,isNewRec = false;
	// ---------------------- //
	private ArrayList<RecordDataObj> RecordInfo = new ArrayList<RecordDataObj>();
	ArrayList<UserObj> UserInfos = new ArrayList<UserObj>();

// =========================================================================
// TODO Activity Life Cycle
// =========================================================================
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle sonicInstance) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(sonicInstance);
		setContentView(R.layout.act_edit_record);

		SetupControls();
	}
	// =========================================================================
	@Override
	protected void onPause() {
		super.onPause();

//		if (player.isPlaying()) {
//
//		}

		//player.stop();
		//player.release();
	}
	// =========================================================================
	@SuppressLint("HandlerLeak")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 1){
			
			updateProgressBar();
			
			if(resultCode == RESULT_OK){
				
				SaveBtn.setEnabled(true);
				
				Bundle values = data.getExtras();
				
				FileF = new File(values.getString("FinalFile"));
				
				isNewRec = true;
				playStop();
				playStart();
				
//				final File fileOne =  new File(values.getString("file1"));
//				
//				long firsDur = values.getLong("firstduration");
//				final String TotalDurOne = RecordUtils.durationToFrames(firsDur);
//				// --------------------------------------------------------------
//				final File fileTwo = new File(values.getString("file2"));
//				
//				long secondDur = values.getLong("secondduration");
//				final String TotalDurTwo = RecordUtils.durationToFrames(secondDur);
//				// --------------------------------------------------------------	
//				final File fileThree = new File(values.getString("file3")); 
//				
//				long thirdDur = values.getLong("thirdduration");
//				final String TotalDurThree = RecordUtils.durationToFrames(thirdDur);
//				// --------------------------------------------------------------
//				String mainDirectory = values.getString("maindirectory");
//				String recordname = RecordInfo.get(Vars.getPlaybackPos()).RECORDNAME;
//				FileF = new File(mainDirectory,"/final_"+recordname+".3gp");
//				
//				//final File file_path = new File(RecordInfo.get(Vars.PlaybackPos).RECORDPATH);
//				//final File file_path2 = new File("mnt/sdcard/MobidicAudio/benkurama/Temp/2_One.3gp");
//				
//				
//				// -----=-----=-----=----- ><
//				 final ProgressDialog dialog = ProgressDialog.show(this, "Please Wait..","Completing Record", true);
//					final Handler handler = new Handler() {
//					   public void handleMessage(Message msg) {
//					      dialog.dismiss();
//					      ///// 2nd if the load finish -----
//					      setupNewRecord();
//						  ///// -----
//					      }};
//					      Thread checkUpdate = new Thread() {  
//					   public void run() {	
//						  /// 1st main activity here... -----
//						  //AudioEditor.combiAudio(fileOne, TotalDurOne, fileOne, TotalDurOne, fileThree,TotalDurThree, FileF);
//						  AudioEditor.combiAudio(fileOne,"0", TotalDurOne, fileTwo,"0", TotalDurTwo, fileThree,"0",TotalDurThree, FileF);
//						  ////// -----
//					      handler.sendEmptyMessage(0);				      
//					      }};
//					      checkUpdate.start();
				
			}
			
			if(resultCode == RESULT_CANCELED){
				
				Utils.MessageBox("New Record is Canceled", this);
			}
		}
	}
// =========================================================================
// TODO onClick View
// =========================================================================
	public void onBack(View v) {
		this.finish();
	}
	// =========================================================================
	@SuppressLint("HandlerLeak")
	public void onRec(View v) {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Edit this part?");
 		alert.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				CreatDirectory();
				firstDur = player.getCurrentPosition();
				TotalDur = player.getDuration();
				nextEdit();
			}
		});
 		alert.setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
 		alert.show(); 

//		 final String framelast = RecordUtils.durationToFrames(firstDur);
//		 
//		 String recordname = RecordInfo.get(Vars.getPlaybackPos()).RECORDNAME;
//		 File1 = new File(MainDirectory,"/1_"+recordname+".3gp");
//		// -----=-----=-----=----- ><
//		 final ProgressDialog dialog = ProgressDialog.show(this, "Please Wait..","Initializing Record Editor", true);
//			final Handler handler = new Handler() {
//			   public void handleMessage(Message msg) {
//			      dialog.dismiss();
//			      ///// 2nd if the load finish -----
//			      nextEdit();
//				  ///// -----
//			      }};
//			      Thread checkUpdate = new Thread() {  
//			   public void run() {	
//				  /// 1st main activity here... -----
//				  AudioEditor.trimAudio(RecordInfo.get(Vars.PlaybackPos).RECORDPATH, "0", framelast, File1);
//				  ////// -----
//			      handler.sendEmptyMessage(0);				      
//			      }};
//			      checkUpdate.start();
 		
	}
	// =========================================================================
	public void onPlay(View v) {

		playStart();
	}
	// =========================================================================
	public void onStop(View v) {

		playStop();
	}
	// =========================================================================
	public void onSave(View v) {
		
		final File fileSource = new File(FileF.getAbsolutePath());
		final File fileDestination = new File(RecordInfo.get(Vars.PlaybackPos).RECORDPATH);
		
		//startActivity(new Intent(this,RecordEditingAct.class));
		// -----=-----=-----=----- ><
		 final ProgressDialog dialog = ProgressDialog.show(this, "Please Wait..","Completing Record", true);
			final Handler handler = new Handler() {
			   public void handleMessage(Message msg) {
			      dialog.dismiss();
			      ///// 2nd if the load finish -----
			      finishing();
				  ///// -----
			      }};
			      Thread checkUpdate = new Thread() {  
			   public void run() {	
				  /// 1st main activity here... -----
				  copyDirectory(fileSource,fileDestination);
				  ////// -----
			      handler.sendEmptyMessage(0);				      
			      }};
			      checkUpdate.start();
	}
// =========================================================================
// TODO Implementation
// =========================================================================
	private Runnable mUpdateTimeTask = new Runnable() {
		// TODO Runnable
		public void run() {
			long totalDuration = player.getDuration();
			long currentDuration = player.getCurrentPosition();

			TotalDuration.setText(""
					+ TimeCon.milliSecondsToTimer(totalDuration));
			CurrentDuration.setText(""
					+ TimeCon.milliSecondsToTimer(currentDuration));

			int progress = (int) (TimeCon.getProgressPercentage(
					currentDuration, totalDuration));
			SeekPlayer.setProgress(progress);

			mHandler.postDelayed(this, 100);
		}
	};
	// =========================================================================	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

	}
	// =========================================================================
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}
	// =========================================================================
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = player.getDuration();
		int currentPosition = TimeCon.progressToTimer(seekBar.getProgress(),totalDuration);

		// forward or backward to certain seconds
		player.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}
	// =========================================================================
	@Override
	public void onCompletion(MediaPlayer mp) {

		playStop();
	}
 // =========================================================================
 // TODO Functions
 // =========================================================================
	public void SetupControls() {

		RecordInfo = Vars.getRecordlistObj();

		TimeCon = new TimeConvertion();

		TotalDuration = (TextView) findViewById(R.id.tvTotalDurationE);
		CurrentDuration = (TextView) findViewById(R.id.tvCurrentDurationE);

		RecordBtn = (Button) findViewById(R.id.btnRecE);
		PlayBtn = (Button) findViewById(R.id.btnPlayE);
		StopBtn = (Button) findViewById(R.id.btnStopE);
		SaveBtn = (Button)findViewById(R.id.btnSaveE);

		SeekPlayer = (SeekBar) findViewById(R.id.sbSeekPlayerE);
		SeekPlayer.setOnSeekBarChangeListener(this);
		SeekPlayer.setEnabled(false);

		player = new MediaPlayer();
		// playerDial = new MediaPlayer();

		SeekPlayer.setProgress(0);
		SeekPlayer.setMax(100);

		// --- Initialize
		RecordBtn.setEnabled(false);
		StopBtn.setEnabled(false);
		SaveBtn.setEnabled(false);

	}
	// =========================================================================
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}
	// =========================================================================
	public void playStart() {

		if (!oncePlayed) {

			try {

				SeekPlayer.setEnabled(true);

				updateProgressBar();

				player.reset();
				if(!isNewRec){
					player.setDataSource(RecordInfo.get(Vars.getPlaybackPos()).RECORDPATH);
				}else{
					player.setDataSource(FileF.getAbsolutePath());
				}
				
				player.prepare();
				player.start();

				PlayBtn.setText("Pause");
				RecordBtn.setEnabled(false);
				StopBtn.setEnabled(true);

				oncePlayed = true;

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {

			if (player.isPlaying()) {

				player.pause();
				PlayBtn.setText("Play");
				RecordBtn.setEnabled(true);

				// ---

				// ---
			} else {

				player.start();
				PlayBtn.setText("Pause");
				RecordBtn.setEnabled(false);
			}

			
		}
	}
	// =========================================================================
	public void playStop() {

		player.seekTo(0);

		player.stop();
		oncePlayed = false;
		PlayBtn.setText("Play");
		StopBtn.setEnabled(false);
		RecordBtn.setEnabled(true);

	}
	// =========================================================================
	public void CreatDirectory() {

		// ------------- Step 1 >>>
		String filePath = "/MobidicAudio/";
		MainDirectory = new File(Environment.getExternalStorageDirectory(),
				filePath);

		if (!MainDirectory.exists()) {
			MainDirectory.mkdir();
		}
		// ------------- Step 2 >>>
		filePath += "/" + Vars.getUsername(this) + "/";
		MainDirectory = new File(Environment.getExternalStorageDirectory(),
				filePath);

		if (!MainDirectory.exists()) {
			MainDirectory.mkdir();
		}
		// ------------- Step 3 >>>
		filePath += "/Temp/";
		MainDirectory = new File(Environment.getExternalStorageDirectory(),
				filePath);

		if (!MainDirectory.exists()) {
			MainDirectory.mkdir();
		}
	}
// =========================================================================
// TODO Functions for Record Dialog
// =========================================================================
	public void nextEdit(){
		
		mHandler.removeCallbacks(mUpdateTimeTask);
		
		Bundle passValue = new Bundle();
		//passValue.putString("value", "Record Test");
		passValue.putString("directory",MainDirectory.getAbsolutePath());
		passValue.putLong("firstduration", firstDur);
		passValue.putLong("totalduration", TotalDur);
		//passValue.putString("file1", File1.getAbsolutePath());
		passValue.putString("file1", RecordInfo.get(Vars.PlaybackPos).RECORDPATH);
		
		Intent RecordInt = new Intent(this,RecordEditingAct.class);
		RecordInt.putExtras(passValue);
		
		startActivityForResult(RecordInt,1);
	}
	// =========================================================================
	@SuppressLint("HandlerLeak")
	public void testingCombi(){
		
		
		final File fileOne =  new File(Environment.getExternalStorageDirectory(),"/MobidicAudio/benkurama/Indefinitely.3gp");
		
		//long firsDur = values.getLong("firstduration");
		final String TotalDurOne = "2000";
		// --------------------------------------------------------------
		final File fileTwo = new File(Environment.getExternalStorageDirectory(),"/MobidicAudio/benkurama/Temp/Indefinitely_-1056981436.3gp");
		
		//long secondDur = 
		final String TotalDurTwo = "2000";
		// --------------------------------------------------------------	
		final File fileThree = new File(Environment.getExternalStorageDirectory(),"/MobidicAudio/benkurama/Nobody.3gp"); 
		
		//long thirdDur = values.getLong("thirdduration");
		final String TotalDurThree = "2000";
		// --------------------------------------------------------------
		String mainDirectory = MainDirectory.getAbsolutePath();
		String recordname = RecordInfo.get(Vars.getPlaybackPos()).RECORDNAME;
		FileF = new File(mainDirectory,"/finalTest_"+recordname+".3gp");
		
		//final File file_path = new File(RecordInfo.get(Vars.PlaybackPos).RECORDPATH);
		//final File file_path2 = new File("mnt/sdcard/MobidicAudio/benkurama/Temp/2_One.3gp");
		
		
		// -----=-----=-----=----- ><
		 final ProgressDialog dialog = ProgressDialog.show(this, "Please Wait..","Completing Record", true);
			final Handler handler = new Handler() {
			   public void handleMessage(Message msg) {
			      dialog.dismiss();
			      ///// 2nd if the load finish -----
			      //setupNewRecord();
				  ///// -----
			      }};
			      Thread checkUpdate = new Thread() {  
			   public void run() {	
				  /// 1st main activity here... -----
				  //AudioEditor.combiAudio(fileOne, TotalDurOne, fileOne, TotalDurOne, fileThree,TotalDurThree, FileF);
				  AudioEditor.combiAudio(fileOne,"0", TotalDurOne, fileTwo,"0", TotalDurTwo, fileThree,"0",TotalDurThree, FileF);
				  ////// -----
			      handler.sendEmptyMessage(0);				      
			      }};
			      checkUpdate.start();
	}
	// =========================================================================
	 public void copyDirectory(File sourceLocation , File targetLocation) {
		  
			try {
				
				InputStream in = new FileInputStream(sourceLocation);
				OutputStream out = new FileOutputStream(targetLocation);
				 // Copy the bits from instream to outstream
		         byte[] buf = new byte[1024];
		         
		         int len;
		         
		         while ((len = in.read(buf)) > 0) {
		             out.write(buf, 0, len);
		         }
		         
		         in.close();
		         out.close();
		         
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	 // =========================================================================
	 public void finishing(){
		 
		 Utils.MessageToast("Edit Record Success", this);
		 FileF.delete();
		 
		 this.finish();
	 }
// =========================================================================
// TODO Final
}
