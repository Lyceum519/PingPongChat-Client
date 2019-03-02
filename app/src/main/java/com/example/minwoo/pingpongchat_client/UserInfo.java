package com.example.minwoo.pingpongchat_client;

import java.io.Serializable;

public class UserInfo implements Serializable {
    public static final String EXTRA = "com.example.minwoo.UserInfo_EXTRA";

    public String personName;
    public String personEmail;
    public String personPhotoUrl;

    public UserInfo (String personName, String personEmail, String personPhotoUrl){
        this.personName = personName;
        this.personEmail = personEmail;
        this.personPhotoUrl = personPhotoUrl;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }
}
