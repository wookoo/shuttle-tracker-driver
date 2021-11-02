package me.synology.wookoo.shuttle_tracker_driver;

import com.google.gson.annotations.SerializedName;

public class rideUploadData {
    @SerializedName(value = "status")
    private boolean status;
    @SerializedName(value = "name")
    private String name;


    public boolean getStatus(){
        return this.status;
    }
    public String getName(){return  this.name;}
}
