package com.example.minwoo.pingpongchat_client;

        import android.net.Uri;

public class UserInfo {
    public String personName;
    public String personEmail;
    public String personPhotoUrl;

    public UserInfo (String personName, String personEmail, String personPhotoUrl){
        this.personName = personName;
        this.personEmail = personEmail;
        this.personPhotoUrl = personPhotoUrl;
    }
}
