package me.synology.wookoo.shuttle_tracker_driver;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitAPI {

    @POST("bus/upload/")
    Call<busUploadData> upload(@Body JSONObject body);


}
