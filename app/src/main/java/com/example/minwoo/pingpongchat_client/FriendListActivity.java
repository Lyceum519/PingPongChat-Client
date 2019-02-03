package com.example.minwoo.pingpongchat_client;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.minwoo.pingpongchat_client.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;

public class FriendListActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            String personPhotoUrl = acct.getPhotoUrl().toString();

            Log.i("GoogleLogin2", "personName=" + personName);
            Log.i("GoogleLogin2", "personEmail=" + personEmail);
            Log.i("GoogleLogin2", "personPhoto=" + personPhotoUrl);
        }

        ArrayList<UserInfo> UserInfoArrayList = new ArrayList<>();
        UserInfoArrayList.add(new UserInfo(acct.getDisplayName(), acct.getEmail(), acct.getPhotoUrl().toString()));

        MyAdapter myAdapter = new MyAdapter(this, UserInfoArrayList);
        mRecyclerView.setAdapter(myAdapter);
    }
}
