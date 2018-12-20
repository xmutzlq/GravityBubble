package com.mygdx.game.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;

/**
 * @author lq.zeng
 * @date 2018/12/13
 */

public class ScreenUtil {
    public static boolean isScreenPortrait(Activity context) {
        return context.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
}
