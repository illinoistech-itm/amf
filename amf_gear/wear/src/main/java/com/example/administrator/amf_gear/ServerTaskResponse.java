package com.example.administrator.amf_gear;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018-06-14.
 */

public interface ServerTaskResponse {
    void onServerTaskCompleted(JSONObject output);
}
