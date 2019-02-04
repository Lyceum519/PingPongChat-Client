package com.example.minwoo.pingpongchat_client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.minwoo.pingpongchat_client.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.Objects;

public class FriendListActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            // 누르고 뗄 때 한번만 인식하도록 하기위해서
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
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

        final ArrayList<UserInfo> UserInfoArrayList = new ArrayList<>();
        UserInfoArrayList.add(new UserInfo(acct.getDisplayName(), acct.getEmail(), acct.getPhotoUrl().toString()));

        MyAdapter myAdapter = new MyAdapter(this, UserInfoArrayList);
        mRecyclerView.setAdapter(myAdapter);

        RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                // 터치한 곳의 View가 RecyclerView 안의 아이템이고 그 아이템의 View가 null이 아니라
                // 정확한 Item의 View를 가져왔고, gestureDetector에서 한번만 누르면 true를 넘기게 구현했으니
                // 한번만 눌려서 그 값이 true가 넘어왔다면
                if (childView != null && gestureDetector.onTouchEvent(e)) {
                    // 현재 터치된 곳의 position을 가져오고
                    int currentPosition = rv.getChildAdapterPosition(childView);
                    // 해당 위치의 Data를 가져옴
                    UserInfo userInfo = UserInfoArrayList.get(currentPosition);
                    // 터치한 항목이 본인이면 프로필 액티비티로 전환
                    if(Objects.equals(acct.getEmail(), userInfo.personEmail)
                    && Objects.equals(acct.getDisplayName(), userInfo.personName)){
                        Intent loginIntent = new Intent(FriendListActivity.this, UserProfileActivity.class);
                        FriendListActivity.this.startActivity(loginIntent);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
        mRecyclerView.addOnItemTouchListener(onItemTouchListener);
    }
}