package com.example.kaeuc.dronemaster;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by kaeuc on 7/13/2016.
 */

public class SendDataToServer extends AsyncTask<String,String,String> {

    private static final String TAG = "SendDataToServer";
    public AsyncResponse callBack = null;
    private Context mContext;



    public interface AsyncResponse {
        void onTaskCompleted(String output);
    }

    public SendDataToServer(Context context){
        this.mContext = context;
        this.callBack = (AsyncResponse) context;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try{
            Scanner scan = new Scanner(result);
            final String response = scan.nextLine();
            if(response.contains("1")){
                String receivedText = "";
                while(scan.hasNext()){
                    receivedText += scan.nextLine() + " ";
                }
                callBack.onTaskCompleted(receivedText);
            }else{
                Log.i("OnPost","Failure");
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }



    @Override
    protected String doInBackground(String... params) {
        String jsonResponse = null;
        String jsonData = params[0];
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try{

            URL url = new URL("http://104.194.106.230:8080/request"/*getString(R.string.server_url)*/);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout( 10000 /*milliseconds*/ );
            connection.setConnectTimeout( 15000 /* milliseconds */ );
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();


            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(jsonData);

            writer.close();

            InputStream inputStream = connection.getInputStream();

            StringBuffer buffer = new StringBuffer();

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
            Log.i(TAG,jsonResponse);
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

        return null;
    }




}