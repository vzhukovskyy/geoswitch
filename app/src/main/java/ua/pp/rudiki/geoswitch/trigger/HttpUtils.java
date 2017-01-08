package ua.pp.rudiki.geoswitch.trigger;


import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    public void sendPostAsync(String url) {
        PostJob job = new PostJob();
        Character separator = (url.indexOf('?') != -1) ? '&' : '?';

        job.execute(url + separator + "access_token=" + GeoSwitchApp.getGoogleSignIn().getToken());
    }

    private class PostJob extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            sendPost(url);
            return "";
        }

        @Override
        protected void onPostExecute(String message) {
        }
    }

    public void sendPost(String url) {
        try {
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

            int responseCode = con.getResponseCode();
            //        System.out.println("\nSending 'POST' request to URL : " + url);
            //        System.out.println("Post parameters : " + urlParameters);
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
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}