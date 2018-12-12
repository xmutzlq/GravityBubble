package com.mygdx.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author lq.zeng
 * @date 2018/12/11
 */

public class BallSprite extends BaseSprite implements Parcelable {
    int color;

    public BallSprite() {}

    protected BallSprite(Parcel in) {
        color = in.readInt();
        size = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(color);
        dest.writeFloat(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BallSprite> CREATOR = new Creator<BallSprite>() {
        @Override
        public BallSprite createFromParcel(Parcel in) {
            return new BallSprite(in);
        }

        @Override
        public BallSprite[] newArray(int size) {
            return new BallSprite[size];
        }
    };
}
