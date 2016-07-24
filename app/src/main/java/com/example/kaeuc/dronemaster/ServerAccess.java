package com.example.kaeuc.dronemaster;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kaeuc on 7/13/2016.
 */

public class ServerAccess extends AsyncTask<String,String,String> {

    private static final String TAG = "ServerAccess";
    public ServerTaskResponse callBack = null;
    private Context mContext;



    public ServerAccess(Context context){
        this.mContext = context;
        this.callBack = (ServerTaskResponse) context;
    }



    @Override
    protected String doInBackground(String... params) {
        String jsonResponse = null;
        String httpMethod = params[0];
        String jsonData = params[1];
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        if(httpMethod == "POST"){
            try{
                /*Server URL*/
                URL url = new URL(mContext.getString(R.string.server_post_url));
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout( 10000 /*milliseconds*/ );
                connection.setConnectTimeout( 15000 /* milliseconds */ );
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                // Send the post data
                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(jsonData);
                writer.close();

                Log.i("JsonData",jsonData);

                //Receives the response
                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                // Empty response
                if(inputStream == null){
                    return null;
                }
                reader =  new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return null;
                    }
                jsonResponse = buffer.toString();
                return jsonResponse;

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        JSONObject jsonResult = null;
        try {
            jsonResult = new JSONObject(result);
            if((jsonResult.getInt("response") == 200) && (jsonResult.getString("method").equalsIgnoreCase("POST"))){
                String address = jsonResult.getString("address");
                final Resources res = mContext.getResources();
                String successText = res.getString(R.string.request_succeed);
                jsonResult.put("address",address);
                jsonResult.put("response",successText + " "+ address);
                jsonResult.put("droneID",jsonResult.getString("droneID"));
                callBack.onServerTaskCompleted(jsonResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




}