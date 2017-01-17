package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class GoogleSignIn {
    private final String TAG = getClass().getSimpleName();
    private static final String SERVER_CLIENT_ID = "815763108077-nb9vs1kk1d7g7k153496c6bajj32nq27.apps.googleusercontent.com";

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private String token;


    public GoogleSignIn(Context appContext) {
        this.mContext = appContext;
        createGoogleClient();
    }

    public void refreshToken(final AsyncResultCallback callback) {
        new Thread( new Runnable() { public void run() {
            doRefreshToken(callback);
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
                .build();
    }

    private void doRefreshToken(final AsyncResultCallback callback) {
        try {
            ConnectionResult result = mGoogleApiClient.blockingConnect();
            if (result.isSuccess()) {
                GoogleSignInResult signInResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient).await();
                if (signInResult != null) {
                    GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
                    if (signInAccount != null) {
                        GeoSwitchApp.getGpsLog().log("Token obtained");

                        token = signInAccount.getIdToken();
                        if (callback != null) {
                            callback.onResult(true);
                            return;
                        }
                    }
                }
            }
        } finally {
            mGoogleApiClient.disconnect();
        }

        GeoSwitchApp.getGpsLog().log("Failed to obtain token");
        callback.onResult(false);
    }
}
