package com.bluestacks.bugzy.models.resp;

import com.google.gson.annotations.SerializedName;

import com.bluestacks.bugzy.models.Request;

public class SearchCasesRequest  {
    private String[] cols;
    private int max;

    @SerializedName("q")
    private String query;

    public SearchCasesRequest(String[] cols, String query) {
        this.cols = cols;
        this.query = query;
    }

    public String[] getCols() {
        return cols;
    }

    public void setCols(String[] cols) {
        this.cols = cols;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}