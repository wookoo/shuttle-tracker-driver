package me.synology.wookoo.shuttle_tracker_driver;

import com.google.gson.annotations.SerializedName;

public class rideUploadData {
    @SerializedName(value = "status")
    private boolean status;
    @SerializedName(value = "name")
    private String name;
    @SerializedName(value = "info")
    private String info;


    public boolean getStatus(){
        return this.status;
    }
    public String getName(){return  this.name;}
    public String getInfo(){return  this.info;}
}
