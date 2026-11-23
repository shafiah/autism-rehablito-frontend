
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class ParentHomeworkRemarkRequest {

    @SerializedName("parentRemarks")
    private String parentRemarks;

    public ParentHomeworkRemarkRequest(String parentRemarks) {
        this.parentRemarks = parentRemarks;
    }

    public String getParentRemarks() {
        return parentRemarks;
    }

    public void setParentRemarks(String parentRemarks) {
        this.parentRemarks = parentRemarks;
    }
}
