package com.jivescribe.mt;

import java.util.ArrayList;

import com.jivescribe.mt.objects.UserObj;
import com.jivescribe.mt.utils.JsonParser;
import com.jivescribe.mt.utils.NetUtils;
import com.jivescribe.mt.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class UserLoginAct extends Activity{
 // =========================================================================
 // TODO Variables
 // =========================================================================	
	private EditText usernameET,passwordET;
	
	private String value = "";
	ArrayList<UserObj> UserInfos = new ArrayList<UserObj>();
 // =========================================================================
 // TODO Activity Life Cycle
 // =========================================================================		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle sonicInstance) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(sonicInstance);
        setContentView(R.layout.act_user_login);
        
        SetupControls();
        
        if(!TextUtils.isEmpty(Vars.getUsername(this))){
        	
        	usernameET.setText(Vars.getUsername(this));
        	passwordET.setText(Vars.getPassword(this));
        }
    }
 // =========================================================================
 // TODO onClick View
 // =========================================================================
    @SuppressLint("HandlerLeak")
	public void onLogin(View v){
    	
    	final String username = usernameET.getText().toString();
    	final String password = passwordET.getText().toString();
    	
    	if(username.length() != 0 && password.length() != 0){ // -- nested 1>
    	
	    	if(NetUtils.isNetworkOn(this)){ // -- nested 2>
	    	
	    	final ProgressDialog dialog = ProgressDialog.show(this, "Please Wait..","Processing...", true);
			final Handler handler = new Handler() {
			   public void handleMessage(Message msg) {
			      dialog.dismiss();
			      ///// 2nd if the load finish -----
			      postData();
				  ///// -----
			      }};
			      Thread checkUpdate = new Thread() {  
			   public void run() {	
				  /// 1st main activity here... -----
				   UserInfos = JsonParser.userValidation(username);
				  ////// -----
			      handler.sendEmptyMessage(0);				      
			      }};
			      checkUpdate.start();
			      
	    	}else{
	    		Utils.MessageBox("No Internet Connection", this);
	    	} // -- nested 2<
    	
    	} else {
    		Utils.MessageBox("Fill all the Textbox", this);
    	} // -- nested 1<
    	
    }
    // =========================================================================
    public void onTest(View v){
    	
    	
    	UserObj testUser = new UserObj();
    	
    	testUser.ID = 1;
    	testUser.NAME = "Alvin";
    	testUser.USERNAME = "benkurama";
    	testUser.PASSWORD = "pass123";
    	testUser.EMAIL = "benkurama@gmail.com";
    	
    	UserInfos.add(testUser);
    	
    	Vars.setUserListObj(UserInfos);
		startActivity(new Intent(this,RecordListAct.class));	//
    }
 // =========================================================================
 // TODO Implementation
 // =========================================================================

 // =========================================================================
 // TODO Functions
 // =========================================================================
    public void SetupControls(){
	   	
    	usernameET = (EditText)findViewById(R.id.etUsername);
    	passwordET = (EditText)findViewById(R.id.etPassword);
    	
    }
 // =========================================================================
    public void postData(){
    	
    	
    	if(UserInfos.get(0).VALID.equals("1")){
    		
    		String password = passwordET.getText().toString();
    		
    		if(UserInfos.get(0).PASSWORD.equals(password)){
    			
    			Vars.setUserListObj(UserInfos);
    			Vars.setUsername(this, UserInfos.get(0).USERNAME);
    			Vars.setPassword(this, UserInfos.get(0).PASSWORD);
    			
    			startActivity(new Intent(this,RecordListAct.class));	//
    		}else{
    			Utils.MessageBox("Password is Incorrect", this);
    		}
    		
    	}else{
    		Utils.MessageBox("Username is Incorrect", this);
    	}
    }
 // =========================================================================
 // TODO Final Destination
}
