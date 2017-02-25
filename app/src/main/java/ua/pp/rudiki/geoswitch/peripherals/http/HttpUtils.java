package ua.pp.rudiki.geoswitch.peripherals.http;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;

public class HttpUtils {
    private final static String TAG = HttpUtils.class.getSimpleName();

    public static void sendGet(String url) {
        try {
            App.getLogger().debug(TAG, "Sending request to URL: " + url);

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //        con.setRequestMethod("GET");
        //        con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            App.getLogger().debug(TAG, "Response code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            App.getLogger().debug(TAG, "Response text: "+response.toString());
        }
        catch (Exception ex) {
            App.getLogger().exception(TAG, ex);
        }
    }

    public static void sendPostAsync(String url, String token, AsyncResultCallback<PostResult> callback) {
        boolean appendToken = (token != null);
        App.getLogger().info(TAG, "Sending POST request to "+url+", appendToken="+appendToken);

        String newUrl;
        if(token != null && !token.isEmpty()) {
            Character separator = (url.indexOf('?') != -1) ? '&' : '?';
            newUrl = url + separator + "access_token=" + App.getGoogleApiClient().getToken();
        } else {
            newUrl = url;
        }

        PostJob job = new PostJob(callback);
        job.execute(newUrl);
    }
}