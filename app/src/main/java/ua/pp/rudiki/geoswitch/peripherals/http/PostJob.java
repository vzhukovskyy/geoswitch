package ua.pp.rudiki.geoswitch.peripherals.http;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;

class PostJob extends AsyncTask<String, Void, String> {
    private final static String TAG = PostJob.class.getSimpleName();

    private AsyncResultCallback<PostResult> listener;

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
            App.getLogger().info(TAG, "Response code: " + responseCode + ", body: " + responseBody);
        } catch (Exception ex) {
            App.getLogger().exception(TAG, ex);
        }

        if(listener != null) {
            PostResult postResult = new PostResult();
            postResult.responseCode = responseCode;
            postResult.responseBody = responseBody;
            listener.onResult(postResult);
        }
    }
}
