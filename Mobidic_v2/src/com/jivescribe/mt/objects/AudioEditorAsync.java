package com.jivescribe.mt.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.jivescribe.mt.utils.AudioEditVars;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.TextView;

public class AudioEditorAsync extends AsyncTask<String, Void, String>{
 // =========================================================================
 // TODO Variables
 // =========================================================================
	int mFileSize;

    int mNumFrames = 0;
    int mMaxFrames = 64;  // This will grow as needed
    int[] mFrameOffsets = new int[mMaxFrames];
    int[] mFrameLens = new int[mMaxFrames];
    int[] mFrameGains = new int[mMaxFrames];
    int mMinGain = 1000000000;
    int mMaxGain = 0;
    int mBitRate = 10;
    int mOffset = 0;
    
    public TextView title;
    public String FirstFrames,LastFrames;
    
    File mInputFile = null;
    File PathFile = null;
    String RecordName = "";
    
//    private ProgressDialog progDial;
//    private Context Core;
    
    public AudioEditorAsync(Context item,TextView text,String frstframes,String lstframes, File path){
    	
    	this.title = text;
    	this.FirstFrames = frstframes;
    	this.LastFrames = lstframes;
    	this.PathFile = path;
    	//this.Core = item;
    }
   // =========================================================================
 	
 	    /** For debugging
 	    public static void main(String[] argv) throws Exception {
 	        File f = new File("");
 	        CheapAMR c = new CheapAMR();
 	        c.ReadFile(f);
 	        c.WriteFile(new File(""),
 	                    0, c.getNumFrames());
 	    } **/
 	
 // =========================================================================
 	private void parse3gpp(InputStream stream, int maxLen)
             throws java.io.IOException {
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
 	void parseAMR(InputStream stream, int maxLen)
             throws java.io.IOException {
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
 	int parseAMRFrame(InputStream stream, int maxLen, int[] prevEner)
             throws java.io.IOException {
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
     void addFrame(int offset, int frameSize, int gain) {
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
    // =========================================================================
     
    @Override
 	protected void onPreExecute(){
 		super.onPreExecute();
 		
// 		progDial = new ProgressDialog(Core);
// 		progDial.setMessage("Processing...");
// 		progDial.setCancelable(false);
// 		progDial.show();
 	}
// TODO Main Code
	@Override
	protected String doInBackground(String... params) {
		
		String post = "Success";
		
	try{
		
		ArrayList<FrameRateObj> FrameArr = new ArrayList<FrameRateObj>();
    	String[] paths = new String[]{params[0]};
		
    	for(int z = 0;z < 1;z++){
    		
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
    	
    		//String FileN = "/1_" + RecordName + ".3gp";
    		
			File outStream = new File(PathFile.getAbsolutePath());
    	
			outStream.createNewFile();
			FileOutputStream out = new FileOutputStream(outStream);
			
			// --
			FileInputStream in = new FileInputStream(FrameArr.get(0).Files);
	        
	        
	        int startFrame = Integer.parseInt(FirstFrames);
	        //int startFrame = 5000;
	        //int numFrames = 12800;
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
	            out.write(buffer, 0, len);
	            pos += len;
	        }
	        
	        
	        in.close();
//	        in2.close();
//	        in3.close();
	        out.close();
    	
	        
	}catch(IOException e){
    	
    }
		return post;
	}
	
	@Override
	protected void onPostExecute(String value){
		
		title.setText(value);
		//progDial.dismiss();
	}
	// =========================================================================
	public void clearAll(){
		
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