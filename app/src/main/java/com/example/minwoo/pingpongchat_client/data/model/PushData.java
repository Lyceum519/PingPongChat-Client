package com.example.minwoo.pingpongchat_client.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonObject;


public class PushData {
    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("priority")
    @Expose
    private String priority;
    @SerializedName("restricted_package_name")
    @Expose
    private String restrictedPackageName;
    @SerializedName("notification")
    @Expose
    private Notification notification;
    @SerializedName("data")
    @Expose
    private Data data;

    private JsonObject jsonObject;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRestrictedPackageName() {
        return restrictedPackageName;
    }

    public void setRestrictedPackageName(String restrictedPackageName) {
        this.restrictedPackageName = restrictedPackageName;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}