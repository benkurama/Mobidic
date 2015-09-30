package com.jivescribe.mt.utils;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.media.AudioManager;

public class RecordUtils {
	// =========================================================================
	public static void setMaxVol(Context core){
		// Get Max Volume:
		// Get the AudioManager
	    AudioManager audioManager = (AudioManager)core.getSystemService(Context.AUDIO_SERVICE);
	    // Set the volume of played media to maximum.
	    audioManager.setStreamVolume (AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
	}
	// =========================================================================
	public static String durationToFrames(long duration){
		
		 int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(duration);
		 int frames = seconds * 200;
		 String frameString = frames+"";
		
		return frameString;
	}
 // =========================================================================
 // TODO Final Destination
}
