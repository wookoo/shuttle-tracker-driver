package me.synology.wookoo.shuttle_tracker_driver;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitAPI {

    @POST("bus/gps/upload/")
    Call<busUploadData> sendGPS(@Body JSONObject body);

    @POST("bus/ride/upload/")
    Call<rideUploadData> sendRide(@Body JSONObject body);

    @POST("bus/check/")
    Call<remainData> check(@Body JSONObject body);


}
