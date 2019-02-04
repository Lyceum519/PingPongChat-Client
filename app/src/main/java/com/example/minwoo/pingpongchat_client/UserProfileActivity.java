package com.example.minwoo.pingpongchat_client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;
    private RetrofitBuilder.PingPongService mPingPongService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        TextView tvPersonName;
        TextView tvPersonEmail;
        ImageView ivPersonPhoto;

        tvPersonName = (TextView) findViewById(R.id.tvPersonName);
        tvPersonEmail = (TextView) findViewById(R.id.tvPersonEmail);
        ivPersonPhoto = (ImageView) findViewById(R.id.ivPersonPhoto);

        RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
        mPingPongService = retrofitBuilder.getService();

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personPhotoUrl = acct.getPhotoUrl().toString();

            tvPersonName.setText(personName);
            tvPersonEmail.setText(personEmail);
            Glide.with(this)
                 .load(personPhotoUrl)
                 .into(ivPersonPhoto);
        } else {
            Toast.makeText(UserProfileActivity.this, "정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void signOut(View view) {
        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        String personEmail = acct.getEmail();
        String personIdToken = acct.getIdToken();
        final JsonObject personData = new JsonObject();

        personData.addProperty("email", personEmail);
        personData.addProperty("token", personIdToken);

        showProgressDialog();
        //Google Logout
        LoginActivity.mGoogleSignInClient
            .signOut()
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        Call<JsonArray> request = mPingPongService.signout(personData);
                        request.enqueue(new Callback<JsonArray>() {
                            @Override
                            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                                //서버 로그아웃 성공
                                hideProgressDialog();
                                Intent loginIntent = new Intent(UserProfileActivity.this, LoginActivity.class);
                                UserProfileActivity.this.startActivity(loginIntent);
                            }

                            @Override
                            public void onFailure(Call<JsonArray> call, Throwable t) {
                                // 서버 로그아웃 실패
                                hideProgressDialog();
                                Log.e("Server Login Fail", t.toString());
                                Toast.makeText(UserProfileActivity.this, "서버 로그아웃 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
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
}
