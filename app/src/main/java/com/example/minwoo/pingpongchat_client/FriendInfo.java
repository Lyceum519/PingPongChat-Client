package com.example.minwoo.pingpongchat_client;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendInfo {
    @SerializedName("email")
    @Expose
    private String friendEmail;
    @SerializedName("name")
    @Expose
    private String friendName;
    @SerializedName("photo")
    @Expose
    private String friendPhoto;

    /**
     * @return The friendEmail
     */
    public String getfriendEmail() {
        return friendEmail;
    }

    /**
     * @param friendEmail The email
     */
    public void setfriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    /**
     * @return The friendName
     */
    public String getFriendName() {
        return friendName;
    }

    /**
     * @param friendName The name
     */
    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    /**
     * @return The friendPhoto
     */
    public String getFriendPhoto() {
        return friendPhoto;
    }

    /**
     * @param friendPhoto The photo
     */
    public void setFriendPhoto(String friendPhoto) {
        this.friendPhoto = friendPhoto;
    }
}