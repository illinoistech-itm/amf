package com.example.kaeuc.dronemaster;

import android.content.Context;
import android.content.res.Resources;
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
                String address = "";
                while(scan.hasNext()){
                    address += scan.nextLine() + " ";
                }
                final Resources res = mContext.getResources();
                String successText = res.getString(R.string.request_succeed);
                result = "";
                result += successText + address;
                callBack.onTaskCompleted(result);
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
            /*Server URL*/
            URL url = new URL(mContext.getString(R.string.server_url));
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout( 10000 /*milliseconds*/ );
            connection.setConnectTimeout( 15000 /* milliseconds */ );
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();




            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(jsonData);

            Log.i("JsonData",jsonData);

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
            Log.i("JsonResponse",jsonResponse);
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