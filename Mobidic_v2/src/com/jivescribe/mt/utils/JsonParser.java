package com.jivescribe.mt.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jivescribe.mt.objects.UserObj;

public class JsonParser {
	// =========================================================================
	 // TODO Message Box | Page: Universal
	 // =========================================================================
		public static ArrayList<UserObj> userValidation(String username){
			
			ArrayList<UserObj> UserInfos = new ArrayList<UserObj>();
			String JSON_KEY_VALID = "validation";
			String JSON_KEY_USER = "users";
			String ValidVal = "";
			
			String URL = "http://10.10."+Configs.IPCon+"/Default.aspx?username="+username+"";
			
			List<NameValuePair> params = new ArrayList<NameValuePair>(); //?
			JsonParsing jsonParser = new JsonParsing();
	        JSONObject json = jsonParser.getJSONFromUrl(URL, "GET", params);
	        
	        try{
	        	
	        	Object Valid = json.get(JSON_KEY_VALID);
	        	ValidVal = Valid.toString();
	        	
	        	if(Valid.equals("1")){
	        		
	        		JSONArray UserData = json.getJSONArray(JSON_KEY_USER);
	        		
	        		for(int x = 0; x < UserData.length(); x++){
	        			
	        			UserObj users = new UserObj();
	        			JSONObject user = UserData.getJSONObject(x);
	        			
	        			users.ID = user.getInt("ID");
	        			users.USERNAME = user.getString("USERNAME");
	        			users.PASSWORD = user.getString("PASSWORD");
	        			users.NAME = user.getString("NAME");
	        			users.EMAIL = user.getString("EMAIL");
	        			
	        			users.VALID = ValidVal;
	        			
	        			UserInfos.add(users);
	        		}
	        		
	        	}else{
	        		
	        		UserObj users = new UserObj();
	        		users.VALID = ValidVal;
	        		UserInfos.add(users);
	        	}
	        	
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	        
	        return UserInfos;
		}
 // =========================================================================
 // TODO Final Destination
}
