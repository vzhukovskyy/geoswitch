package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class GeoSwitchGoogleApiClient {
    private final String TAG = getClass().getSimpleName();
    private static final String SERVER_CLIENT_ID = "815763108077-nb9vs1kk1d7g7k153496c6bajj32nq27.apps.googleusercontent.com";

    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private String token;


    public GeoSwitchGoogleApiClient(Context appContext) {
        this.context = appContext;
        createGoogleClient();
    }

    public void refreshToken(final AsyncResultCallback<Boolean> callback) {
        new Thread( new Runnable() { public void run() {
                boolean isSuccess = doRefreshToken();
                if (callback != null) {
                    callback.onResult(isSuccess);
                }
            }}).start();
    }

    public void requestLastLocation(final AsyncResultCallback<Location> callback) {
        new Thread( new Runnable() { public void run() {
                Location lastLocation = doRetrieveLastLocation();
                if (callback != null) {
                    callback.onResult(lastLocation);
                }
            }}).start();
    }

    public String getToken() {
        return token;
    }

    // Google sign-in
    // This is initial sign-in into application. After successful sign-in what is needed is refreshing token
    // right before making POST request to the server
    // Here I am using separate googleApiClient other than GeoSwitchGoogleApiClient because sign-in includes user operations,
    // Leveraging automanage facility is clearly better choice than handling it by my own

    public void startSigninForResult(FragmentActivity activity, int requestCode) {
        if(true) return;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.e(TAG, "Connection to google sign-in service failed");
            }
        };

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, connectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, requestCode);

        Log.i(TAG, "Sign-in for foreground activity initiated");
    }


    public void handleSignInResult(GoogleSignInResult result) {
        Log.i(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.i(TAG, "Signed in for foreground activity as "+account.getEmail());
        } else {
            Log.e(TAG, "Sign-in failed");
        }
    }

    // Private

    private void createGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();
    }

    private synchronized boolean doRefreshToken() {
        try {
            GeoSwitchApp.getLogger().log("Connecting to Google API for an updated token");

            ConnectionResult result = mGoogleApiClient.blockingConnect();
            if (result.isSuccess()) {
                GoogleSignInResult signInResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient).await();
                if (signInResult != null) {
                    GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
                    if (signInAccount != null) {
                        GeoSwitchApp.getLogger().log("Token obtained");

                        token = signInAccount.getIdToken();
                        return true; // finally block will be executed before return
                    }
                }
            }
        } finally {
            mGoogleApiClient.disconnect();
        }

        GeoSwitchApp.getLogger().log("Failed to obtain token");
        return false;
    }

    private synchronized Location doRetrieveLastLocation() {
        try {
            GeoSwitchApp.getLogger().log("Connecting to Google API for last location");

            ConnectionResult result = mGoogleApiClient.blockingConnect();
            if (result.isSuccess()) {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                GeoSwitchApp.getLogger().log("Last location obtained: "+lastLocation);
                return lastLocation;
            }
        } finally {
            mGoogleApiClient.disconnect();
        }

        GeoSwitchApp.getLogger().log("Failed to obtain last location");
        return null;
    }
}
