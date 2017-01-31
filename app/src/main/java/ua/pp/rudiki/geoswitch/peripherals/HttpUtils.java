package ua.pp.rudiki.geoswitch.peripherals;


import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class HttpUtils {

    public void sendGet(String url) {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //        con.setRequestMethod("GET");
        //        con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class PostResult {
        public int responseCode;
        public String responseBody;
    };

    public void sendPostAsync(String url, AsyncResultCallback<PostResult> callback) {
        String newUrl = url;
        PostJob job = new PostJob(callback);

        if(GeoSwitchApp.getPreferences().getAppendToken()) {
            Character separator = (url.indexOf('?') != -1) ? '&' : '?';
            newUrl = url + separator + "access_token=" + GeoSwitchApp.getGoogleSignIn().getToken();
        }

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
                GeoSwitchApp.getGpsLog().log("Sending POST");

                URL obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                //        con.setRequestProperty("User-Agent", USER_AGENT);
                //        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                //wr.writeBytes(urlParameters);
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
                GeoSwitchApp.getGpsLog().log("Response code: " + responseCode + ", body: " + responseBody);
            } catch (Exception ex) {
                ex.printStackTrace();
                GeoSwitchApp.getGpsLog().log("Exception " + ex);
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