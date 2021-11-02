package me.synology.wookoo.shuttle_tracker_driver;

import com.google.gson.annotations.SerializedName;

public class busUploadData {
    @SerializedName(value = "status")
    private boolean status;

    public boolean getStatus(){
        return this.status;
    }


}
