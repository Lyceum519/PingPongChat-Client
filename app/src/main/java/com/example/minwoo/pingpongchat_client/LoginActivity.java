package com.example.minwoo.pingpongchat_client;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.constraint.Constraints.TAG;

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
    // FCM token
    private static String firebaseCloudMessagingToken;

    private static final int MESSAGE_PERMISSION_GRANTED = 101;
    private static final int MESSAGE_PERMISSION_DENIED = 102;

    public MainHandler mainHandler = new MainHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        showPermissionDialog();

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

        //FCM Token 얻기
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        firebaseCloudMessagingToken = task.getResult().getToken();
                        // Log
                        Log.d("FCM token : ", firebaseCloudMessagingToken);
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
                JsonObject personData = createPersonObject(account);
                requestResult(personData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Signed out, show unauthenticated UI.
            //Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
            hideProgressDialog();
            Log.e("GoogleLogin ", "Google silent sign in failed");
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            showProgressDialog();

            // Signed in Google successfully, try to sign in to Server.
            JsonObject personData = createPersonObject(account);
            requestResult(personData);
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

    private JsonObject createPersonObject(GoogleSignInAccount account) {
        String personName = account.getDisplayName();
        String personEmail = account.getEmail();
        String personIdToken = account.getIdToken();
        String personPhotoUrl = account.getPhotoUrl().toString();
        JsonObject personData = new JsonObject();
        personData.addProperty("name", personName);
        personData.addProperty("email", personEmail);
        personData.addProperty("token", personIdToken);
        personData.addProperty("photo", personPhotoUrl);
        personData.addProperty("client_token", firebaseCloudMessagingToken);

        Log.i("GoogleLogin", "personName=" + personName);
        Log.i("GoogleLogin", "personEmail=" + personEmail);
        Log.i("GoogleLogin", "personIdToken=" + personIdToken);
        Log.i("GoogleLogin", "personPhotoUrl=" + personPhotoUrl);
        Log.i("GoogleLogin", "personFCMToken=" + firebaseCloudMessagingToken);

        return personData;
    }

    private void requestResult(JsonObject personData) {
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
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PERMISSION_GRANTED:
                    Log.d("Permission", "Permission Granted");
                    break;
                case MESSAGE_PERMISSION_DENIED:
                    Log.d("Permission", "Permission Denied");
                    finish();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private void showPermissionDialog() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Log.d("Permission", "Permission Granted");
                mainHandler.sendEmptyMessage(MESSAGE_PERMISSION_GRANTED);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Log.d("Permission", "Permission Denied");
                mainHandler.sendEmptyMessage(MESSAGE_PERMISSION_DENIED);
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET)
                .check();
    }
}
