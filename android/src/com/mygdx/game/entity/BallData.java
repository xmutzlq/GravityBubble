package com.mygdx.game.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author lq.zeng
 * @date 2018/12/17
 */

public class BallData implements Parcelable {
    public int color; //颜色
    public float size; //大小
    public int role; //角色

    public BallData() {

    }

    protected BallData(Parcel in) {
        color = in.readInt();
        size = in.readFloat();
        role = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(color);
        dest.writeFloat(size);
        dest.writeInt(role);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BallData> CREATOR = new Creator<BallData>() {
        @Override
        public BallData createFromParcel(Parcel in) {
            return new BallData(in);
        }

        @Override
        public BallData[] newArray(int size) {
            return new BallData[size];
        }
    };
}
