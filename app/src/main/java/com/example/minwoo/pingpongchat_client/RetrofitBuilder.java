package com.example.minwoo.pingpongchat_client;

import android.content.Intent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RetrofitBuilder {
    Retrofit retrofit;

    public RetrofitBuilder() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://172.20.10.2:7001/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public RetrofitBuilder.PingPongService getService() {
        RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
        Retrofit retrofit = retrofitBuilder.retrofit;
        RetrofitBuilder.PingPongService service = retrofit.create(RetrofitBuilder.PingPongService.class);

        return service;
    }

    public interface PingPongService {
        @GET("users")
        Call<List<FriendInfo>> getUsers();

        @Multipart
        @POST("record")
        Call<ResponseBody> sendRecord(@Part("description") RequestBody description,
                                      @Part MultipartBody.Part file,
                                      @Query("from") String from,
                                      @Query("to") String to);

        @GET("records/who")
        Call<ResponseBody> getRecordFrom(@Query("from") String from,
                                         @Query("to") String to);

        @POST("signin")
        Call<JsonArray> signin(@Body JsonObject data);

        @POST("signout")
        Call<JsonArray> signout(@Body JsonObject data);
    }
}
