package com.test.smarttouchlayout_master;

import android.os.Parcel;
import android.os.Parcelable;

public class DataBean implements Parcelable {
    public int resId;
    public int localX;
    public int localY;
    public int width;
    public int height;

    public DataBean(){}

    protected DataBean(Parcel in) {
        resId = in.readInt();
        localX = in.readInt();
        localY = in.readInt();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(resId);
        dest.writeInt(localX);
        dest.writeInt(localY);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
        @Override
        public DataBean createFromParcel(Parcel in) {
            return new DataBean(in);
        }

        @Override
        public DataBean[] newArray(int size) {
            return new DataBean[size];
        }
    };
}
