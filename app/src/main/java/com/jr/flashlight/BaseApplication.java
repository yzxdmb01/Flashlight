package com.jr.flashlight;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2016-11-21.
 */

public class BaseApplication extends Application {
    private static BaseApplication baseApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }

    public static Context getContext() {
        return baseApplication;
    }
}
