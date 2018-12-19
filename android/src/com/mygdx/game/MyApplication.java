package com.mygdx.game;

import android.app.Application;
import android.support.multidex.MultiDex;

public class MyApplication extends Application {

    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MultiDex.install(this);
    }
}
