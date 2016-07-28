package com.example.kaeuc.dronemaster;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by kaeuc on 7/24/2016.
 */

public class DroneLocation extends AsyncTask<String,Void,String> {
    Context parentContext;
    private static final String TAG = "DroneLocation";
    public DroneLocationResponse callBack = null;


    public DroneLocation(Context parentContext) {
        this.parentContext = parentContext;
        this.callBack = (DroneLocationResponse) parentContext;
    }

    @Override
    protected String doInBackground(String... params) {
        String jsonResponse = null;
        HttpURLConnection connection = null;
        String appID = params[0];
        String ipAddress = params[1];
        String query = "";
        URL url;
            try{
                query = String.format("instanceID=%s",
                        URLEncoder.encode(appID,"UTF-8"));
                if(ipAddress.isEmpty())
                    url = new URL(parentContext.getString(R.string.server_post_url));
                else
                    url = new URL("http://"+ipAddress+":8080?"+query);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset","UTF-8");
                // Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                jsonResponse = response.toString();
                Log.d("ServerResponse/",jsonResponse);
                return jsonResponse;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject response = new JSONObject(s);
            Log.i(TAG,"Drone location requested");
            callBack.onDroneLocationResponse(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }
}
