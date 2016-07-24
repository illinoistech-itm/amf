package com.example.kaeuc.dronemaster;

import org.json.JSONObject;

/**
 * Created by kaeuc on 7/22/2016.
 */

public interface ServerTaskResponse {
    void onServerTaskCompleted(JSONObject output);
}
