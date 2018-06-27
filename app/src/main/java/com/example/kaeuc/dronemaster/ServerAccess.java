package com.example.kaeuc.dronemaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.internal.gmsg.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static java.lang.System.in;


/**
 * Created by kaeuc on 7/13/2016.
 */


public class ServerAccess extends AsyncTask<String,String,String> {



        private static final String TAG = "ServerAccess";
        public ServerTaskResponse callBack = null;
        private Context mContext;
//        private MapsActivity main;

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
            /*String server_post_url = main.server_post_url;
            String server_get_url = main.server_get_url;*/

            try {
                URL url = new URL(mContext.getString(R.string.server_post_url));
                /*URL url = new URL();*/
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

    /*public static HttpsURLConnection setUpHttpsConnection(HttpsURLConnection urlString, Context context)
    {
        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = context.getResources().openRawResource(R.raw.localhost);
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, tmf.getTrustManagers(), null);

            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(context.getString(R.string.server_post_url));
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());

            return urlConnection;
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
    }*/

        /*HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("example.com",session);
            }
        };*/

 /*   public static SSLSocketFactory getPinnedCertSslSocketFactory(Context context) {
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = context.getResources().openRawResource(R.raw.localhost);
            Certificate ca = null;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } catch (CertificateException e) {
                e.printStackTrace();
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            if (ca == null) {
                return null;
            }
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext= SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/







    //previous code
    /*@Override
    protected String doInBackground(String ... params) {
        String jsonResponse = null;
        String jsonData = params[0];
        String ipAddress = params[1];
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        *//*Server URL*//*
        URL url = null;
            try{
                    if(ipAddress.isEmpty())
                        url = new URL(mContext.getString(R.string.server_post_url));
                else
                    url = new URL("http://"+ipAddress+":8080");
                Log.i(TAG,"Request sent to: "+ url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout( 10000 *//*milliseconds*//* );
                connection.setConnectTimeout( 10000 *//* milliseconds *//* );
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();


                // Send the post data
                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(jsonData);
                writer.close();

                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return null;

                Log.i("JsonDataSent",jsonData);

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
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
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
    }*/



}