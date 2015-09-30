package com.jivescribe.mt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.jivescribe.mt.objects.FrameRateObj;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.TextView;

public class AudioEditor {
 // =========================================================================
 // TODO Variables
 // =========================================================================
	private static int mFileSize;
// ---------------------- //
    private static int mNumFrames = 0;
    private static int mMaxFrames = 64;  // This will grow as needed
    private static int[] mFrameOffsets = new int[mMaxFrames];
    private static int[] mFrameLens = new int[mMaxFrames];
    private static int[] mFrameGains = new int[mMaxFrames];
    private static int mMinGain = 1000000000;
    private static int mMaxGain = 0;
    private static int mBitRate = 10;
    private static int mOffset = 0;
    
    public static TextView title;
    public static String FirstFrames;

	public static String LastFrames;
    
	private static File mInputFile = null;
	private static File PathFile = null;
	private String RecordName = "";
	// ---------------------- //
	private static File SourceOne,SourceTwo,SourceThree;
	private static File OutputFile;
	private static String DurationOne,DurationTwo,DurationThree;
	// ---------------------- //
	private static String Dur1st,Dur2nd,Dur3rd;
// =========================================================================
	public static void trimAudio(String recordpath,String frstframes,String lstframes, File path){
		// -----=-----=-----=----- ><
    	FirstFrames = frstframes;
    	LastFrames = lstframes;
    	PathFile = path;
		// -----=-----=-----=----- ><
		try{
			
			ArrayList<FrameRateObj> FrameArr = new ArrayList<FrameRateObj>();
	    	String[] paths = new String[]{recordpath};
			
	    	for(int z = 0;z < paths.length; z++){
	    		
	    		FrameRateObj frame = new FrameRateObj();
	    	
		    	mInputFile = new File(paths[z]);
			    // No need to handle filesizes larger than can fit in a 32-bit int
		        mFileSize = (int)mInputFile.length();
		
		        if (mFileSize < 128) {
		            throw new java.io.IOException("File too small to parse");
		        }
		
		        FileInputStream stream = new FileInputStream(mInputFile);
		        byte[] header = new byte[12];
		        stream.read(header, 0, 6);
		        
		        mOffset += 6;
		        if (header[0] == '#' &&  header[1] == '!' &&  header[2] == 'A' &&  header[3] == 'M' &&
		            header[4] == 'R' &&  header[5] == '\n') {
		            parseAMR(stream, mFileSize - 6);
		        }
		
		        stream.read(header, 6, 6);
		        mOffset += 6;
		        
		        if (header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p' &&
		            header[8] == '3' && header[9] == 'g' && header[10] == 'p' && header[11] == '4') {
		
			        int boxLen = ((0xff & header[0]) << 24) | ((0xff & header[1]) << 16) | ((0xff & header[2]) << 8) |
			            ((0xff & header[3]));
			
			        if (boxLen >= 4 && boxLen <= mFileSize - 8) {
			            stream.skip(boxLen - 12);
			            mOffset += boxLen - 12;
			        }
			
			        parse3gpp(stream, mFileSize - boxLen);
			        
		        }
		        
		        int[] FrameOffsetsX = new int[64];
		        int[] FrameLensX = new int[64];
		        // ----
		        FrameOffsetsX = mFrameOffsets;
		        FrameLensX = mFrameLens;
		        
		        frame.FrameOffSet = FrameOffsetsX;
		        frame.FrameLen = FrameLensX;
		        frame.Files = mInputFile;
		        
		        FrameArr.add(frame);
	        
	        clearAll();
	        
	    	}
	    	/// -------------------------------------
	    	
				File outStream = new File(PathFile.getAbsolutePath());
	    	
				outStream.createNewFile();
				FileOutputStream out = new FileOutputStream(outStream);
				
				// --
				FileInputStream in = new FileInputStream(FrameArr.get(0).Files);
		        
		        
		        int startFrame = 0;
		        int numFrames = Integer.parseInt(LastFrames);
		
		        byte[] header2 = new byte[6];
		        header2[0] = '#';
		        header2[1] = '!';
		        header2[2] = 'A';
		        header2[3] = 'M';
		        header2[4] = 'R';
		        header2[5] = '\n';
		        out.write(header2, 0, 6);
		        
		        int maxFrameLen = 0;
		        
		        for (int i = 0; i < numFrames; i++) {
		            if (FrameArr.get(0).FrameLen[startFrame + i] > maxFrameLen)
		                maxFrameLen = FrameArr.get(0).FrameLen[startFrame + i];
		        }
		        
		        byte[] buffer = new byte[maxFrameLen];
		        int pos = 0;
		        // benkurama added codes:
		        int frameStart = Integer.parseInt(FirstFrames);
		        
		        for (int i = 0; i < numFrames; i++) {
		        	
		            int skip = FrameArr.get(0).FrameOffSet[startFrame + i] - pos;
		            int len = FrameArr.get(0).FrameLen[startFrame + i];
		            
		            if (skip < 0) {
		                continue;
		            }
		            
		            if (skip > 0) {
		                in.skip(skip);
		                pos += skip;
		            }
		            
		            in.read(buffer, 0, len);
		            // -- benkurama added codes >>
		            if (frameStart <= i){
		            	out.write(buffer, 0, len);
		            }
		            // -- <<
		            pos += len;
		        }
		        
		        in.close();
		        out.close();
		        
		        clearAll();
		        
		}catch(IOException e){
	    	
	    }
	}
	// =========================================================================
	public static void combiAudio(File sourceOne,String Dur1stf,String durationOne,File sourceTwo,String Dur2ndf, String durationTwo,File sourceThree, String Dur3rdf , String durationThree,File fileOuput){
		// TODO Combi Audio
		SourceOne = sourceOne;
		SourceTwo = sourceTwo;
		SourceThree = sourceThree;
		
		DurationOne = durationOne;
		DurationTwo = durationTwo;
		DurationThree = durationThree;
		
		Dur1st = Dur1stf;
		Dur2nd = Dur2ndf;
		Dur3rd = Dur3rdf;
		
		OutputFile = fileOuput;
		
		try{
			
			ArrayList<FrameRateObj> FrameArr = new ArrayList<FrameRateObj>();
	    	String[] paths = new String[]{SourceOne.getAbsoluteFile().toString(),SourceTwo.getAbsoluteFile().toString(),SourceThree.getAbsoluteFile().toString()};
	    	//String[] paths = new String[]{SourceOne.getAbsoluteFile().toString(),SourceTwo.getAbsoluteFile().toString()};
			
	    	for(int z = 0;z < paths.length; z++){
	    		
	    		FrameRateObj frame = new FrameRateObj();
	    	
		    	mInputFile = new File(paths[z]);
			    // No need to handle filesizes larger than can fit in a 32-bit int
		        mFileSize = (int)mInputFile.length();
		
		        if (mFileSize < 128) {
		            throw new java.io.IOException("File too small to parse");
		        }
		
		        FileInputStream stream = new FileInputStream(mInputFile);
		        byte[] header = new byte[12];
		        stream.read(header, 0, 6);
		        
		        mOffset += 6;
		        if (header[0] == '#' &&  header[1] == '!' &&  header[2] == 'A' &&  header[3] == 'M' &&
		            header[4] == 'R' &&  header[5] == '\n') {
		            parseAMR(stream, mFileSize - 6);
		        }
		
		        stream.read(header, 6, 6);
		        mOffset += 6;
		        
		        if (header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p' &&
		            header[8] == '3' && header[9] == 'g' && header[10] == 'p' && header[11] == '4') {
		
			        int boxLen = ((0xff & header[0]) << 24) | ((0xff & header[1]) << 16) | ((0xff & header[2]) << 8) |
			            ((0xff & header[3]));
			
			        if (boxLen >= 4 && boxLen <= mFileSize - 8) {
			            stream.skip(boxLen - 12);
			            mOffset += boxLen - 12;
			        }
			
			        parse3gpp(stream, mFileSize - boxLen);
			        
		        }
		        
		        int[] FrameOffsetsX = new int[64];
		        int[] FrameLensX = new int[64];
		        // ----
		        FrameOffsetsX = mFrameOffsets;
		        FrameLensX = mFrameLens;
		        
		        frame.FrameOffSet = FrameOffsetsX;
		        frame.FrameLen = FrameLensX;
		        frame.Files = mInputFile;
		        
		        FrameArr.add(frame);
	        
	        clearAll();
	    	}
	    	/// -------------------------------------
	    	
	    	File outStream = new File(OutputFile.getAbsolutePath());
	    	
			outStream.createNewFile();
			FileOutputStream out = new FileOutputStream(outStream);
			
			String[] durations = new String[]{DurationOne,DurationTwo,DurationThree};
			String[] initDur = new String [] {Dur1st,Dur2nd,Dur3rd};
			//String[] durations = new String[]{DurationOne,DurationTwo};
			
			// --
			
			//for(int z = 0; z < durations.length; z++){
				
				FileInputStream in = new FileInputStream(FrameArr.get(0).Files);
		    // TODO Current Coding    
		        
				
		        int startFrame = 0;
		        int numFrames = Integer.parseInt(durations[0]);
		
		        byte[] header2 = new byte[6];
		        header2[0] = '#';
		        header2[1] = '!';
		        header2[2] = 'A';
		        header2[3] = 'M';
		        header2[4] = 'R';
		        header2[5] = '\n';
		        out.write(header2, 0, 6);
		        
//		        byte[] header2 = new byte[32];
//		        header2[0] = '#';
//		        header2[1] = '!';
//		        header2[2] = 'A';
//		        header2[3] = 'M';
//		        header2[4] = 'R';
//		        header2[5] = '\n';
//		        header2[6] = '<';
//		        header2[7] = 0x04;
//		        header2[8] = '3';
//		        header2[9] = 't';
//		        header2[10] = '@';
//		        header2[11] = '7';
//		        header2[12] = 'a';
//		        header2[13] = (byte) 0xC7;
//		        header2[14] = (byte) 0xCC;
//		        header2[15] = (byte) 0xEE;
//		        
//		        header2[16] = (byte) 0xC6;
//		        header2[17] = (byte) 0x3A;
//		        header2[18] = (byte) 0xF7;
//		        header2[19] = '"';
//		        header2[20] = 'v';
//		        header2[21] = 'R';
//		        header2[22] = (byte) 0xC0;
//		        header2[23] = (byte) 0x0;
//		        header2[24] = 'N';
//		        header2[25] = (byte) 0xD7;
//		        header2[26] = 'k';
//		        header2[27] = (byte) 0x30;
//		        header2[28] = 'y';
//		        header2[29] = (byte) 0xF0;
//		        header2[30] = (byte) 0x00;
//		        header2[31] = (byte) 0x00;
//		        
//		        out.write(header2, 0, 32);
		        
		        //  --------------------------------
		        
//		        byte[] header2 = new byte[32];
//		        header2[0] = 0x0;
//		        header2[1] = 0x0;
//		        header2[2] = 0x0;
//		        header2[3] = 0x18;
//		        header2[4] = 'f';
//		        header2[5] = 't';
//		        header2[6] = 'y';
//		        header2[7] = 'p';
//		        header2[8] = '3';
//		        header2[9] = 'g';
//		        header2[10] = 'p';
//		        header2[11] = '4';
//		        header2[12] = 0x0;
//		        header2[13] = 0x0;
//		        header2[14] = 0x0;
//		        header2[15] = 0x0;
//		        header2[16] = 'i';
//		        header2[17] = 's';
//		        header2[18] = 'o';
//		        header2[19] = 'm';
//		        header2[20] = '3';
//		        header2[21] = 'g';
//		        header2[22] = 'p';
//		        header2[23] = '4';
//		        header2[24] = 0x0;
//		        header2[25] = 0x01;
//		        header2[26] = 0x06;
//		        header2[27] = (byte) 'è';
//		        header2[28] = 'm';
//		        header2[29] = 'd';
//		        header2[30] = 'a';
//		        header2[31] = 't';
//		        
//		        out.write(header2, 0, 32);
		
		        int maxFrameLen = 0;
		        
		        for (int i = 0; i < numFrames; i++) {
		            if (FrameArr.get(0).FrameLen[startFrame + i] > maxFrameLen)
		                maxFrameLen = FrameArr.get(0).FrameLen[startFrame + i];
		        }
		        
		        
		        byte[] buffer = new byte[maxFrameLen];
		        int pos = 0;
		        // benkurama added codes:
		        int frameStart = Integer.parseInt(initDur[0]);
		        
		        for (int i = 0; i < numFrames; i++) {
		        	
		            int skip = FrameArr.get(0).FrameOffSet[startFrame + i] - pos;
		            int len = FrameArr.get(0).FrameLen[startFrame + i];
		            
		            if (skip < 0) {
		                continue;
		            }
		            
		            
		            if (skip > 0) {
		                in.skip(skip);
		                pos += skip;
		            }
		            
		            in.read(buffer, 0, len);
		            // -- benkurama added codes >>
		            if (frameStart <= i){
		            	out.write(buffer, 0, len);
		            }
		            // -- <<
		            pos += len;
		            
		        }
				
		        //in.close();
			//}
			
			//out.close();
	        
	        // --------------------------------------------------------------
	        
	        FileInputStream in2 = new FileInputStream(FrameArr.get(1).Files);
	        
	        int startFrame2 = 0;
	        int numFrames2 = Integer.parseInt(durations[1]);
	        

	        int maxFrameLen2 = 0;
	        for (int i = 0; i < numFrames2; i++) {
	            if (FrameArr.get(1).FrameLen[startFrame2 + i] > maxFrameLen2)
	                maxFrameLen2 = FrameArr.get(1).FrameLen[startFrame2 + i];
	        }
	        
	        byte[] buffer2 = new byte[maxFrameLen2];
	        int pos2 = 0;
	        int frameStart2 = Integer.parseInt(initDur[1]);
	        
	        for (int i = 0; i < numFrames2; i++) {
	        	
	            int skip2 = FrameArr.get(1).FrameOffSet[startFrame2 + i] - pos2;
	            int len2 = FrameArr.get(1).FrameLen[startFrame2 + i];
	            
	            if (skip2 < 0) {
	                continue;
	            }
	            
	            if (skip2 > 0) {
	                in2.skip(skip2);
	                pos2 += skip2;
	            }
	            
	            in2.read(buffer2, 0, len2);
	            // -- benkurama added codes >>
	            if (frameStart2 <= i){
	            	out.write(buffer2, 0, len2);
	            }
	            // -- <<
	            pos2 += len2;
	        }
	        
	        // --------------------------------------------------------------
	        
	        FileInputStream in3 = new FileInputStream(FrameArr.get(2).Files);
	        
	        int startFrame3 = 0;
	        int numFrames3 = Integer.parseInt(durations[2]);
	        
	
	        int maxFrameLen3 = 0;
	        for (int i = 0; i < numFrames3; i++) {
	            if (FrameArr.get(2).FrameLen[startFrame3 + i] > maxFrameLen3)
	                maxFrameLen3 = FrameArr.get(2).FrameLen[startFrame3 + i];
	        }
	        
	        byte[] buffer3 = new byte[maxFrameLen3];
	        int pos3 = 0;
	        int frameStart3 = Integer.parseInt(initDur[2]);
	        
	        for (int i = 0; i < numFrames3; i++) {
	            int skip3 = FrameArr.get(2).FrameOffSet[startFrame3 + i] - pos3;
	            int len3 = FrameArr.get(2).FrameLen[startFrame3 + i];
	            if (skip3 < 0) {
	                continue;
	            }
	            if (skip3 > 0) {
	                in3.skip(skip3);
	                pos3 += skip3;
	            }
	            in3.read(buffer3, 0, len3);
	         // -- benkurama added codes >>
	            if (frameStart3 <= i){
	            	out.write(buffer3, 0, len3);
	            }
	            // -- <<
	            pos3 += len3;
	        }
	        // -----------------------------
	        in.close();
	        in2.close();
	        in3.close();
	        
	        out.close();
	        
	        //clearAll();
	    	
	    	}catch(IOException e){
		    	
		    }
		
		
	}
 // =========================================================================
 // TODO Main Functions
 // =========================================================================
	 	private static void parse3gpp(InputStream stream, int maxLen) throws java.io.IOException {
	 		
	         if (maxLen < 8)
	             return;

	         byte[] boxHeader = new byte[8];
	         stream.read(boxHeader, 0, 8);
	         mOffset += 8;
	         
	         int boxLen =
	             ((0xff & boxHeader[0]) << 24) |
	             ((0xff & boxHeader[1]) << 16) |
	             ((0xff & boxHeader[2]) << 8) |
	             ((0xff & boxHeader[3]));

	         if (boxLen > maxLen || boxLen <= 0)
	             return;

	         if (boxHeader[4] == 'm' &&
	             boxHeader[5] == 'd' &&
	             boxHeader[6] == 'a' &&
	             boxHeader[7] == 't') {
	             parseAMR(stream, boxLen);
	             return;
	         }

		 	stream.skip(boxLen - 8);
		 	mOffset += (boxLen - 8);

		 	parse3gpp(stream, maxLen - boxLen);
	     }
	 // =========================================================================
	 	static void parseAMR(InputStream stream, int maxLen)  throws java.io.IOException {
	 		
	         int[] prevEner = new int[4];
	         for (int i = 0; i < 4; i++) {
	             prevEner[i] = 0;
	         }

	         int[] prevEnerMR122 = new int[4];
	         for (int i = 0; i < 4; i++) {
	             prevEnerMR122[i] = -2381;
	         }

	         int originalMaxLen = maxLen;
	         int bytesTotal = 0;
	         while (maxLen > 0) {
	             int bytesConsumed = parseAMRFrame(stream, maxLen, prevEner);
	             bytesTotal += bytesConsumed;
	             maxLen -= bytesConsumed;
	         }
	     }
	 	// =========================================================================
	 	static int parseAMRFrame(InputStream stream, int maxLen, int[] prevEner)  throws java.io.IOException {
	 		
	         int frameOffset = mOffset;
	         byte[] frameTypeHeader = new byte[1];
	         stream.read(frameTypeHeader, 0, 1);
	         mOffset += 1;
	         int frameType = ((0xff & frameTypeHeader[0]) >> 3) % 0x0F;
	         int frameQuality = ((0xff & frameTypeHeader[0]) >> 2) & 0x01;
	         int blockSize = AudioEditVars.BLOCK_SIZES[frameType];

	         if (blockSize + 1 > maxLen) {
	             // We can't read the full frame, so consume the remaining
	             // bytes to end processing the AMR stream.
	             return maxLen;
	         }

	         if (blockSize == 0) {
	             return 1;
	         }

	         byte[] v = new byte[blockSize];
	         stream.read(v, 0, blockSize);
	         mOffset += blockSize;

	         int[] bits = new int[blockSize * 8];
	         int ii = 0;
	         int value = 0xff & v[ii];
	         for (int i = 0; i < blockSize * 8; i++) {
	             bits[i] = ((value & 0x80) >> 7);
	             value <<= 1;
	             if ((i & 0x07) == 0x07 && i < blockSize * 8 - 1) {
	                 ii += 1;
	                 value = 0xff & v[ii];
	             }
	         }
	         // =========================================================================
	 	int[] gain;
	         switch (frameType) {
	 	case 0:
	             mBitRate = 5;
	             gain = new int[4];
	             gain[0] =
	                 0x01 * bits[28] +
	                 0x02 * bits[29] +
	                 0x04 * bits[30] +
	                 0x08 * bits[31] +
	                 0x10 * bits[46] +
	                 0x20 * bits[47] +
	 		0x40 * bits[48] +
	 		0x80 * bits[49];
	             gain[1] = gain[0];
	             gain[2] =
	                 0x01 * bits[32] +
	                 0x02 * bits[33] +
	                 0x04 * bits[34] +
	                 0x08 * bits[35] +
	                 0x10 * bits[40] +
	                 0x20 * bits[41] +
	                 0x40 * bits[42] +
	                 0x80 * bits[43];
	             gain[3] = gain[2];

	             for (int i = 0; i < 4; i++) {
	 		int index = gain[i] * 4 + (i & 1) * 2 + 1;
	 		int gFac = AudioEditVars.GAIN_FAC_MR475[index];

	                 double log2 = Math.log(gFac) / Math.log(2);
	                 int exp = (int)log2;
	                 int frac = (int)((log2 - exp) * 32768);

	 		exp -= 12;
	 		int tmp = exp * 49320;
	 		tmp += ((frac * 24660) >> 15) * 2;
	 		int quaEner = ((tmp * 8192) + 0x8000) >> 16;

	                 int gcode0 =
	                     (385963008 +
	                      prevEner[0] * 5571 +
	                      prevEner[1] * 4751 +
	                      prevEner[2] * 2785 +
	                      prevEner[3] * 1556) >> 15;

	                 prevEner[3] = prevEner[2];
	                 prevEner[2] = prevEner[1];
	                 prevEner[1] = prevEner[0];
	                 prevEner[0] = quaEner;

	                 int frameGainEstimate = (gcode0 * gFac) >> 24;

	                 addFrame(frameOffset, blockSize + 1, frameGainEstimate);
	             }

	 	    break;

	         case 1:
	             mBitRate = 5;
	             gain = new int[4];
	             gain[0] =
	                 0x01 * bits[24] +
	                 0x02 * bits[25] +
	                 0x04 * bits[26] +
	                 0x08 * bits[36] +
	                 0x10 * bits[45] +
	                 0x20 * bits[55];
	             gain[1] =
	                 0x01 * bits[27] +
	                 0x02 * bits[28] +
	                 0x04 * bits[29] +
	                 0x08 * bits[37] +
	                 0x10 * bits[46] +
	                 0x20 * bits[56];
	             gain[2] =
	                 0x01 * bits[30] +
	                 0x02 * bits[31] +
	                 0x04 * bits[32] +
	                 0x08 * bits[38] +
	                 0x10 * bits[47] +
	                 0x20 * bits[57];
	             gain[3] =
	                 0x01 * bits[33] +
	                 0x02 * bits[34] +
	                 0x04 * bits[35] +
	                 0x08 * bits[39] +
	                 0x10 * bits[48] +
	                 0x20 * bits[58];

	             for (int i = 0; i < 4; i++) {
	                 int gcode0 =
	                     (385963008 +
	                      prevEner[0] * 5571 +
	                      prevEner[1] * 4751 +
	                      prevEner[2] * 2785 +
	                      prevEner[3] * 1556) >> 15;
	                 int quaEner = AudioEditVars.QUA_ENER_MR515[gain[i]];
	                 int gFac = AudioEditVars.GAIN_FAC_MR515[gain[i]];

	                 prevEner[3] = prevEner[2];
	                 prevEner[2] = prevEner[1];
	                 prevEner[1] = prevEner[0];
	                 prevEner[0] = quaEner;

	                 int frameGainEstimate = (gcode0 * gFac) >> 24;

	                 addFrame(frameOffset, blockSize + 1, frameGainEstimate);
	             }

	             break;
	         case 7:
	             mBitRate = 12;
	             int[] adaptiveIndex = new int[4];
	             int[] adaptiveGain = new int[4];
	             int[] fixedGain = new int[4];
	             int[][] pulse = new int[4][];
	             for (int i = 0; i < 4; i++) {
	                 pulse[i] = new int[10];
	             }
	             
	             AudioEditVars.getMR122Params(bits, adaptiveIndex, adaptiveGain, fixedGain, pulse);

	             int T0 = 0;
	             for (int subframe = 0; subframe < 4; subframe++) {
	                 int[] code = new int[40];
	                 for (int i = 0; i < 40; i++) {
	                     code[i] = 0;
	                 }

	                 int sign;
	                 for (int j = 0; j < 5; j++) {
	                     if (((pulse[subframe][j] >> 3) & 1) == 0) {
	                         sign = 4096;
	                     } else {
	                         sign = -4096;
	                     }

	                     int pos1 = j + AudioEditVars.GRAY[pulse[subframe][j] & 7] * 5;
	                     int pos2 = j + AudioEditVars.GRAY[pulse[subframe][j + 5] & 7] * 5;
	                     code[pos1] = sign;
	                     if (pos2 < pos1) {
	                         sign = -sign;
	                     }
	                     code[pos2] = code[pos2] + sign;
	                 }

	                 int index = adaptiveIndex[subframe];

	                 if (subframe == 0 || subframe == 2) {
	                     if (index < 463) {
	                         T0 = (index + 5) / 6 + 17;
	                     } else {
	                         T0 = index - 368;
	                     }
	                 } else {
	                     int pitMin = 18;
	                     int pitMax = 143;
	                     int T0Min = T0 - 5;
	                     if (T0Min < pitMin) {
	                         T0Min = pitMin;
	                     }
	                     int T0Max = T0Min + 9;
	                     if (T0Max > pitMax) {
	                         T0Max = pitMax;
	                         T0Min = T0Max - 9;
	                     }
	                     T0 = T0Min + (index + 5) / 6 - 1;
	                 }

	                 int pitSharp =
	                     (AudioEditVars.QUA_GAIN_PITCH[adaptiveGain[subframe]] >> 2) << 2;
	                 if (pitSharp > 16383) {
	                     pitSharp = 32767;
	                 } else {
	                     pitSharp *= 2;
	                 }

	                 for (int j = T0; j < 40; j++) {
	                     code[j] += (code[j - T0] * pitSharp) >> 15;
	                 }
	             
	                 int enerCode = 0;
	                 for (int j = 0; j < 40; j++) {
	                     enerCode += code[j] * code[j];
	                 }

	                 if ((0x3fffffff <= enerCode) || (enerCode < 0)) {
	                     enerCode = 0x7fffffff;
	                 } else {
	                     enerCode *= 2;
	                 }
	                 enerCode = ((enerCode + 0x8000) >> 16) * 52428;

	                 double log2 = Math.log(enerCode) / Math.log(2);
	                 int exp = (int)log2;
	                 int frac = (int)((log2 - exp) * 32768);
	                 enerCode = ((exp - 30) << 16) + (frac * 2);

	                 int ener =
	                     prevEner[0] * 44 +
	                     prevEner[1] * 37 +
	                     prevEner[2] * 22 +
	                     prevEner[3] * 12;

	                 ener = 2 * ener + 783741;
	                 ener = (ener - enerCode) / 2;

	                 int expGCode = ener >> 16;
	                 int fracGCode = (ener >> 1) - (expGCode << 15);

	                 int gCode0 = (int)
	                     (Math.pow(2.0, expGCode + (fracGCode / 32768.0)) + 0.5);

	                 if (gCode0 <= 2047) {
	                     gCode0 = gCode0 << 4;
	                 } else {
	                     gCode0 = 32767;
	                 }

	                 index = fixedGain[subframe];

	                 int gainCode =
	                     ((gCode0 * AudioEditVars.QUA_GAIN_CODE[3 * index]) >> 15) << 1;

	                 if ((gainCode & 0xFFFF8000) != 0) {
	                     gainCode = 32767;
	                 }

	                 int frameGainEstimate = gainCode;

	                 addFrame(frameOffset, blockSize + 1, frameGainEstimate);

	                 int quaEnerMR122 = AudioEditVars.QUA_GAIN_CODE[3 * index + 1];
	                 prevEner[3] = prevEner[2];
	                 prevEner[2] = prevEner[1];
	                 prevEner[1] = prevEner[0];
	                 prevEner[0] = quaEnerMR122;
	             }
	             break;

	         default:
	             System.out.println("Unsupported frame type: " + frameType);
	             addFrame(frameOffset, blockSize + 1, 1);
	             break;
	         }

	         // Return number of bytes consumed
	         return blockSize + 1;
	     }
	 	// =========================================================================
	     static void addFrame(int offset, int frameSize, int gain) {
	    	 
	         mFrameOffsets[mNumFrames] = offset;
	         mFrameLens[mNumFrames] = frameSize;
	         mFrameGains[mNumFrames] = gain;
	         if (gain < mMinGain)
	             mMinGain = gain;
	         if (gain > mMaxGain)
	             mMaxGain = gain;

	         mNumFrames++;
	         if (mNumFrames == mMaxFrames) {
	             int newMaxFrames = mMaxFrames * 2;

	             int[] newOffsets = new int[newMaxFrames];
	             int[] newLens = new int[newMaxFrames];
	             int[] newGains = new int[newMaxFrames];
	             for (int i = 0; i < mNumFrames; i++) {
	                 newOffsets[i] = mFrameOffsets[i];
	                 newLens[i] = mFrameLens[i];
	                 newGains[i] = mFrameGains[i];
	             }
	             mFrameOffsets = newOffsets;
	             mFrameLens = newLens;
	             mFrameGains = newGains;
	             mMaxFrames = newMaxFrames;
	         }
	     }
// =========================================================================
	     public static void clearAll(){
	 		
			 mFileSize = 0;
		     mNumFrames = 0;
		     mMaxFrames = 64;  // This will grow as needed

		     mMinGain = 1000000000;
		     mMaxGain = 0;
		     mBitRate = 10;
		     mOffset = 0;
		}
// =========================================================================
// TODO Final
}
