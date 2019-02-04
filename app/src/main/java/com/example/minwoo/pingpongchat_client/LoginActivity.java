package com.example.minwoo.pingpongchat_client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements OnConnectionFailedListener {
    // 구글로그인 result 상수
    private static final int RC_SIGN_IN = 900;
    // 구글SignIn클라이언트
    public static GoogleSignInClient mGoogleSignInClient;
    // 구글  로그인 버튼
    public SignInButton signInButton;
    // 구글api클라이언트
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private RetrofitBuilder.PingPongService mPingPongService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.btn_googleSignIn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
        mPingPongService = retrofitBuilder.getService();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.btn_googleSignIn:
                        signIn();
                        break;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d("TAG", "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResultOnStart(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResultOnStart(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 구글로그인 버튼 응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResultOnStart(GoogleSignInResult result) {
        Log.d("TAG", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                // Signed in Google successfully, try to sign in to Server.
                String personName = account.getDisplayName();
                String personEmail = account.getEmail();
                String personId = account.getId();
                String personIdToken = account.getIdToken();
                JsonObject personData = new JsonObject();
                personData.addProperty("email", personEmail);
                personData.addProperty("token", personIdToken);

                Log.i("GoogleLogin", "personName=" + personName);
                Log.i("GoogleLogin", "personEmail=" + personEmail);
                Log.i("GoogleLogin", "personId=" + personId);
                Log.i("GoogleLogin", "personIdToken=" + personIdToken);

                Call<JsonArray> request = mPingPongService.signin(personData);

                request.enqueue(new Callback<JsonArray>() {
                    @Override
                    public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                        //서버 로그인 성공
                        hideProgressDialog();
                        //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                        //Log.d("Server Login Success", response.body().toString());
                        Intent loginIntent = new Intent(LoginActivity.this, FriendListActivity.class);
                        LoginActivity.this.startActivity(loginIntent);
                    }

                    @Override
                    public void onFailure(Call<JsonArray> call, Throwable t) {
                        // 서버 로그인 실패
                        hideProgressDialog();
                        Log.e("Server Login Fail", t.toString());
                        Toast.makeText(LoginActivity.this, "서버 로그인 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Signed out, show unauthenticated UI.
            //Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
            hideProgressDialog();
            Log.e("GoogleLogin ","Google silent sign in failed" );
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            showProgressDialog();

            // Signed in Google successfully, try to sign in to Server.
            String personName = account.getDisplayName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            String personIdToken = account.getIdToken();
            JsonObject personData = new JsonObject();
            personData.addProperty("email", personEmail);
            personData.addProperty("token", personIdToken);

            Log.i("GoogleLogin", "personName=" + personName);
            Log.i("GoogleLogin", "personEmail=" + personEmail);
            Log.i("GoogleLogin", "personId=" + personId);
            Log.i("GoogleLogin", "personIdToken=" + personIdToken);

            Call<JsonArray> request = mPingPongService.signin(personData);

            request.enqueue(new Callback<JsonArray>() {
                @Override
                public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                    //서버 로그인 성공
                    hideProgressDialog();
                    //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                    //Log.d("Server Login Success", response.body().toString());
                    Intent loginIntent = new Intent(LoginActivity.this, FriendListActivity.class);
                    LoginActivity.this.startActivity(loginIntent);
                }

                @Override
                public void onFailure(Call<JsonArray> call, Throwable t) {
                    // 서버 로그인 실패
                    hideProgressDialog();
                    Log.e("Server Login Fail", t.toString());
                    Toast.makeText(LoginActivity.this, "서버 로그인 실패", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }
}
