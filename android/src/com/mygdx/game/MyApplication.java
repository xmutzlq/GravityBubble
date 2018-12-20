package com.mygdx.game;

import android.app.Application;

import com.mygdx.game.util.Utils;
//import android.support.multidex.MultiDex;

public class MyApplication extends Application {

    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Utils.init(this);
//        MultiDex.install(this);
    }
}
