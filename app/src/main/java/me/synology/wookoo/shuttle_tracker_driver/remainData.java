package me.synology.wookoo.shuttle_tracker_driver;

import com.google.gson.annotations.SerializedName;

public class remainData {
    @SerializedName(value = "status")
    private boolean status;
    @SerializedName(value = "tags")
    private String tags;
    @SerializedName(value = "remain")
    private int remain;

    public boolean getStatus(){
        return this.status;
    }


    public String getTags() {
        return tags;
    }

    public int getRemain() {
        return remain;
    }
}
