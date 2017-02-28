package ua.pp.rudiki.geoswitch.peripherals.http;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

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

        URL urlObj;
        try {
            urlObj = new URL(url);
        }
        catch(MalformedURLException e) {
            passResultToListener(HttpUtils.CONNECTION_RESULT_MALFORMED_URL);
            return;
        }

        HttpURLConnection httpUrlConnection;
        try {
            URLConnection urlConnection = urlObj.openConnection();
            if(!(urlConnection instanceof HttpURLConnection)) {
                passResultToListener(HttpUtils.CONNECTION_RESULT_UNSUPPORTED_PROTOCOL);
                return;
            }

            httpUrlConnection = (HttpURLConnection) urlConnection;
        }
        catch(IOException e) {
            passResultToListener(HttpUtils.CONNECTION_RESULT_FAILED_TO_OPEN_CONNECTION);
            return;
        }

        try {
            httpUrlConnection.setRequestMethod("POST");
            //        httpUrlConnection.setRequestProperty("User-Agent", USER_AGENT);
            //        httpUrlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            // Send post request
            httpUrlConnection.setDoOutput(true);
        }
        catch(ProtocolException e) {
            // not possible
        }

        try {
            OutputStream outputStream = httpUrlConnection.getOutputStream();
            DataOutputStream wr = new DataOutputStream(outputStream);
            wr.flush();
            wr.close();
        }
        catch(UnknownHostException e) {
            passResultToListener(HttpUtils.CONNECTION_RESULT_UNKNOWN_HOST);
            return;
        }
        catch(IOException e) {
            passResultToListener(HttpUtils.CONNECTION_RESULT_WRITE_ERROR);
            return;
        }

        StringBuffer response = null;
        InputStream inputStream = null;
        try {
            inputStream = httpUrlConnection.getInputStream();
        }
        catch(IOException e) {
            passResultToListener(httpUrlConnection);
            return;
        }

        try {
            if (inputStream != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                response = new StringBuffer();

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }

            responseCode = httpUrlConnection.getResponseCode();
            responseBody = (response != null) ? response.toString() : null;
            passResultToListener(responseCode, responseBody);
        }
        catch (IOException e) {
            passResultToListener(httpUrlConnection);
        }
    }

    void passResultToListener(HttpURLConnection urlConnection) {
        int responseCode;
        try {
            responseCode = urlConnection.getResponseCode();
        }
        catch(IOException e) {
            responseCode = HttpUtils.CONNECTION_RESULT_READ_ERROR;
        }

        passResultToListener(responseCode);
    }

    void passResultToListener(int responseCode) {
        passResultToListener(responseCode, null);
    }

    void passResultToListener(int responseCode, String responseBody) {
        if(listener != null) {
            PostResult postResult = new PostResult();
            postResult.responseCode = responseCode;
            postResult.responseBody = responseBody;
            listener.onResult(postResult);
        }
    }
}
