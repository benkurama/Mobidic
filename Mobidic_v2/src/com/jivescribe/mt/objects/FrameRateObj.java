package com.jivescribe.mt.objects;

import java.io.File;

public class FrameRateObj {
	public int[] FrameOffSet;
	public int[] FrameLen;
	public File Files;
	
	public FrameRateObj(){
		
		this.FrameOffSet = new int[64];
		this.FrameLen = new int[64];
		this.Files = null;
	}
}
