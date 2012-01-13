/* This class is implements REST primitives */

package lab.tutorial.restclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RestComm {
	
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
	private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    // GET Method 
    public static JSONObject restGet(String url) throws JSONException, ClientProtocolException, ConnectTimeoutException
    {
    	Log.i("Rest",url);
    	
        HttpClient httpclient = new DefaultHttpClient();
 
        // Prepare a request object
        HttpGet httpget = new HttpGet(url); 
        httpget.setHeader("Accept", "*/*");
 
        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            // Examine the response status
            Log.i("Rest",response.getStatusLine().toString());
 
            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release
            
            if (entity != null) {
 
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);
                Log.i("Rest",result);
 
                JSONObject json=null;
                
                // A Simple JSONObject Creation
                json=new JSONObject(result);
                
                // Closing the input stream will trigger connection release
                instream.close();
                
                return json;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Restcomm", "Exceptie in restGet");
        }
        
        return null;
    }
    
    // PUT Method
    public static JSONObject restPut(String url) throws JSONException, ClientProtocolException, ConnectTimeoutException
    {
    	Log.i("Rest",url);
    	
        HttpClient httpclient = new DefaultHttpClient();
 
        // Prepare a request object
        HttpPut httpput = new HttpPut(url);
 
        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpput);
            // Examine the response status
            Log.i("Rest",response.getStatusLine().toString());
 
            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release
            
            if (entity != null) {
 
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);
                Log.i("Rest",result);
 
                JSONObject json=null;
                
                // A Simple JSONObject Creation
                json=new JSONObject(result);
                
                // Closing the input stream will trigger connection release
                instream.close();
                
                return json;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        return null;
    }
}