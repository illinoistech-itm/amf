package com.example.administrator.amf_gear;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

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
 * Created by Administrator on 2018-06-14.
 */

public class ServerAccess extends AsyncTask<String,String,String> {
    private static final String TAG = "ServerAccess";
    public ServerTaskResponse callBack = null;
    private Context mContext;


    public ServerAccess(Context context){
        this.mContext = context;
        this.callBack = (ServerTaskResponse) context;
    }

    ProgressDialog progressDialog;

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected String doInBackground(String... params) {
        String JsonResponse = null;
        String JsonDATA = params[0];
        String ipAddress = params[1];
        BufferedReader reader = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(mContext.getString(R.string.server_post_url));
            connection = (HttpURLConnection) url.openConnection();

                /*getPinnedCertSslSocketFactory(mContext);*/
//                connection.setHostnameVerifier(hostnameVerifier);

                /*setUpHttpsConnection(connection,mContext);*/
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            // is output buffer writter
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            //set headers and method
            Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(JsonDATA);
            // json data
            writer.close();

            InputStream inputStream = connection.getInputStream();

            //input stream
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine + "\n");
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            JsonResponse = buffer.toString();
            return JsonResponse;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (progressDialog != null)
            progressDialog.dismiss();
        super.onPostExecute(result);
        JSONObject jsonResult = null;

        try {
            jsonResult = new JSONObject(result);
            if((jsonResult.getInt("RESPONSE") == 200)){
                String address = jsonResult.getString("ADDRESS");
                final Resources res = mContext.getResources();
                String successText = res.getString(R.string.request_succeed);
                jsonResult.put("address",address);
                jsonResult.put("response",successText + " "+ address);
                jsonResult.put("result",1);
                callBack.onServerTaskCompleted(jsonResult);
            }else if(jsonResult.getInt("RESPONSE") == -1){
                String failText = mContext.getString(R.string.request_busy);
                jsonResult.put("result",-1);
                jsonResult.put("response",failText);
                callBack.onServerTaskCompleted(jsonResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        //Do something with result
    }
}
