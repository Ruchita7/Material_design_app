package com.example.xyzreader.remote;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;

    public static final String LOG_TAG=Config.class.getSimpleName();
    static {
        URL url = null;
        try {
            url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json" );
        } catch (MalformedURLException ignored) {
           Log.e(LOG_TAG,"MalformedURLException "+ignored.getMessage());
        }

        BASE_URL = url;
    }
}
