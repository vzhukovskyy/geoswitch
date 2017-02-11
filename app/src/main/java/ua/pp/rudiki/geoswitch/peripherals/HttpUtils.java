package ua.pp.rudiki.geoswitch.peripherals;


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class HttpUtils {
    final String TAG = this.getClass().getSimpleName();

    public void sendGet(String url) {
        try {
            Log.d(TAG, "Sending request to URL: " + url);

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //        con.setRequestMethod("GET");
        //        con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            Log.d(TAG, "Response code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d(TAG, "Response text: "+response.toString());
        }
        catch (Exception ex) {
            Log.d(TAG, ex.getMessage(), ex);
        }
    }

    public class PostResult {
        public int responseCode;
        public String responseBody;
    };

    public void sendPostAsync(String url, String token, AsyncResultCallback<PostResult> callback) {
        boolean appendToken = (token != null);
        GeoSwitchApp.getLogger().log("Sending POST request to "+url+", appendToken="+appendToken);

        String newUrl;
        if(token != null && !token.isEmpty()) {
            Character separator = (url.indexOf('?') != -1) ? '&' : '?';
            newUrl = url + separator + "access_token=" + GeoSwitchApp.getGeoSwitchGoogleApiClient().getToken();
        } else {
            newUrl = url;
        }

        PostJob job = new PostJob(callback);
        job.execute(newUrl);
    }

    private class PostJob extends AsyncTask<String, Void, String> {

        AsyncResultCallback<PostResult> listener;

        public PostJob(AsyncResultCallback<PostResult> listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            sendPost(url);
            return "";
        }

        @Override
        protected void onPostExecute(String message) {
        }

        public void sendPost(String url) {
            int responseCode = 0;
            String responseBody = null;

            try {
                URL obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                //        con.setRequestProperty("User-Agent", USER_AGENT);
                //        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.flush();
                wr.close();

                StringBuffer response = null;
                InputStream inputStream = null;
                try {
                    inputStream = con.getInputStream();
                } catch (FileNotFoundException e1) {
                }

                if (inputStream != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    String inputLine;
                    response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }

                responseCode = con.getResponseCode();
                responseBody = (response != null) ? response.toString() : "<null>";
                GeoSwitchApp.getLogger().log("Response code: " + responseCode + ", body: " + responseBody);
            } catch (Exception ex) {
                GeoSwitchApp.getLogger().log(ex);
            }

            if(listener != null) {
                PostResult postResult = new PostResult();
                postResult.responseCode = responseCode;
                postResult.responseBody = responseBody;
                listener.onResult(postResult);
            }
        }
    }

}