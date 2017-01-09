package ua.pp.rudiki.geoswitch;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

// based on http://stackoverflow.com/questions/34900956/silent-sign-in-to-retrieve-token-with-googleapiclient

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

    public void silentLogin() {
        new Thread( new Runnable() { public void run() {
            doSilentLogin();
        }}).start();
    }

    public String getToken() {
        return token;
    }

    private void createGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        System.out.println("onConnectionFailed  = " + connectionResult);
                        onSilentLoginFinished(null);
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        System.out.println("onConnected bundle = " + bundle);
                        onSilentLoginFinished(null);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        System.out.println("onConnectionSuspended i = " + i);
                        onSilentLoginFinished(null);
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void doSilentLogin() {
        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult != null) {
            if (pendingResult.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Using cached credentials");
                GoogleSignInResult signInResult = pendingResult.get();
                onSilentLoginFinished(signInResult);
            } else {
                Log.d(TAG, "Need to re-login, setting result callback");

                ConnectionResult result = mGoogleApiClient.blockingConnect();

                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        Log.d(TAG,"googleSignInResult = " + googleSignInResult);
                        onSilentLoginFinished(googleSignInResult);

                        mGoogleApiClient.disconnect();
                    }
                });
            }
        } else {
            onSilentLoginFinished(null);
        }

    }

    private void onSilentLoginFinished(GoogleSignInResult signInResult) {
        Log.d(TAG, "GoogleLoginIdToken.onSilentLoginFinished");
        if (signInResult != null) {
            GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
            if (signInAccount != null) {
                String emailAddress = signInAccount.getEmail();
                token = signInAccount.getIdToken();
                Log.d(TAG, "token = " + token);
                Log.d(TAG, "emailAddress = " + emailAddress);
            }
        }
    }
}