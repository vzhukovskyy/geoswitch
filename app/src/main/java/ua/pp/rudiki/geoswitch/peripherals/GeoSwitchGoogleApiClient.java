package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class GeoSwitchGoogleApiClient {
    private final String TAG = getClass().getSimpleName();
    private static final String SERVER_CLIENT_ID = "815763108077-nb9vs1kk1d7g7k153496c6bajj32nq27.apps.googleusercontent.com";

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private String token;


    public GeoSwitchGoogleApiClient(Context appContext) {
        this.mContext = appContext;
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

    private void createGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
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
                GeoSwitchApp.getLogger().log("Last location obtained");
                return lastLocation;
            }
        } finally {
            mGoogleApiClient.disconnect();
        }

        GeoSwitchApp.getLogger().log("Failed to obtain last location");
        return null;
    }

}
