package com.trackersurvey.httpconnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PostGroupInfo extends Thread{
	private Handler mHandler;
	private String url,userId,deviceId,type,key;
	private HttpPost httpRequest;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();  
	
	public PostGroupInfo(Handler mHandler, String url,String userId,String deviceId,String type) {
		
		
		this.mHandler = mHandler;
		this.url=url;
		this.userId=userId;
		this.deviceId=deviceId;
		this.type=type;
		
		params.add(new BasicNameValuePair("userId", userId));  
        params.add(new BasicNameValuePair("deviceId", deviceId)); 
        params.add(new BasicNameValuePair("requestType",type));
	}
	public PostGroupInfo(Handler mHandler, String url,String userId,String deviceId,String type,String key) {
		
		
		this.mHandler = mHandler;
		this.url=url;
		this.userId=userId;
		this.deviceId=deviceId;
		this.type=type;
		this.key=key;
		
		params.add(new BasicNameValuePair("userId", userId));  
        params.add(new BasicNameValuePair("deviceId", deviceId)); 
        params.add(new BasicNameValuePair("key", key));
        params.add(new BasicNameValuePair("requestType",type));
	}
    public void run() {
    	
        
        Post();
	   }
    private void Post(){
    	Message msg = Message.obtain();
        try {  
            HttpEntity httpEntity = new UrlEncodedFormEntity(params,"utf-8");  
            httpRequest = new HttpPost(url);
            httpRequest.setEntity(httpEntity);  
            
            HttpClient httpClient = new DefaultHttpClient();  
            
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,8000);//连接时间
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,5000);//数据传输时间
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
              
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
                BufferedReader bin = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String result = bin.readLine();	
                Log.i("trailadapter", type+",readLine:"+result); 
                if(result!=null){
                    if(result.equals("fail")){
                		msg.what = 1;
                	}
                   
                	else{
                	msg.what = 0;
                	}
                    msg.obj = result;
     			    mHandler.sendMessage(msg);
                }else{
                	msg.what = 1;
                	msg.obj = result;
     			    mHandler.sendMessage(msg);
                }
            }else{  
            	 
            	msg.what = 10;
            	 
 			    msg.obj = "提交失败!";
 			    mHandler.sendMessage(msg);	 
            }  
        } catch (UnsupportedEncodingException e) {  
            // TODO Auto-generated catch block 
        	msg.what = 10;
			msg.obj = "提交失败！";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } catch (ClientProtocolException e) {  
            // TODO Auto-generated catch block 
        	msg.what = 10;
			msg.obj = "提交失败！";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
        	msg.what = 10;
			msg.obj = "提交失败！";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } 
    }
}

