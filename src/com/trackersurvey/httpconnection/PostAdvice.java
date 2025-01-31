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

import com.trackersurvey.helper.Common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PostAdvice extends Thread  {
	private Handler mHandler;
	private String url,adviceData,deviceId;
	
	private HttpPost httpRequest;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();  
	
	public PostAdvice(Handler mHandler, String url,String adviceData,String deviceId)
	{
		this.mHandler = mHandler;
		this.url=url;
		this.adviceData=adviceData;
	    this.deviceId=deviceId;
	}
	public void run()
	{
		//把资料分隔开 时间,地点,建议
		String rd[]=adviceData.split("分隔符");
		params.add(new BasicNameValuePair("userId", rd[0]));
		//Log.i("userId", rd[0]);
		params.add(new BasicNameValuePair("createTime", rd[1])); 
		//Log.i("createTime", rd[1]);
		params.add(new BasicNameValuePair("contents", rd[2]));  
		//Log.i("contents", rd[2]);
		params.add(new BasicNameValuePair("picCount", rd[3]));
		params.add(new BasicNameValuePair("deviceId", deviceId));
		//Log.i("picCount", rd[3]);
		
	        Post();
		
	}
	public void Post()
	{
	
		Message msg = Message.obtain();
        try {  
        	//Log.i("LogDemo", "建立连接");
            HttpEntity httpEntity = new UrlEncodedFormEntity(params,"utf-8");  
            httpRequest = new HttpPost(url);
            httpRequest.setEntity(httpEntity);  
              
            HttpClient httpClient = new DefaultHttpClient();  
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
             
            //Log.i("LogDemoCode", httpResponse.getStatusLine().getStatusCode()+"");  
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
            	  
                BufferedReader bin = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String result = bin.readLine();	
                //Log.i("LogDemo", "Postadvice,readLine:"+result);  
                if(result!=null){
                 if(result.equals("ok"))
                	{
                
                	msg.what = 0;
                	}
                	else{
                		
                		msg.what = 1;
                	}
                    msg.obj = result;
     			    mHandler.sendMessage(msg);
                }else{
                	msg.what = 1;
                	msg.obj = result;
            	
     			    mHandler.sendMessage(msg);
                }
                
            }
            
            else{  
            	//Log.i("LogDemo", "连接失败"); 
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
